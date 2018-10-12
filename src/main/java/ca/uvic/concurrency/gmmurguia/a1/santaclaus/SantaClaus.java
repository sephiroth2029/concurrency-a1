package ca.uvic.concurrency.gmmurguia.a1.santaclaus;

import ca.uvic.concurrency.gmmurguia.a1.ExecutableSolution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SantaClaus extends ExecutableSolution implements Runnable {

    public static final int NUM_REINDEER = 9;
    private static Logger logger = LogManager.getLogger();

    final AtomicInteger reindeerReady = new AtomicInteger(0);

    private final AtomicInteger elvesInNeed = new AtomicInteger(0);

    final AtomicInteger totalElves = new AtomicInteger(0);

    final AtomicInteger years = new AtomicInteger(0);

    Object hut = new Object();

    Object christmas = new Object();

    Object endOfChristmas = new Object();

    Object santasShop = new Object();

    Object santasAvailability = new Object();

    private ConcurrentLinkedQueue<Reindeer> reindeers = new ConcurrentLinkedQueue<>();

    private ConcurrentLinkedQueue<Elf> elves = new ConcurrentLinkedQueue<>();

    boolean christmasTime = true;

    boolean santaHelpingElves = false;

    boolean elvesDone = false;

    boolean reindeerDone = false;

    CountDownLatch hutLatch;

    @Override
    public void run() {
        while (true) {
            if (reindeerReady.get() == 9) {
                synchronized (christmas) {
                    christmasTime = true;
                }
                final CountDownLatch christmasLatch = new CountDownLatch(NUM_REINDEER);
                prepareSleigh(christmasLatch);
                waitForChristmasToEnd(christmasLatch);
            } else if (elvesInNeed.get() == 3) {
                helpElves();
            }

            synchronized (endOfChristmas) {
                logger.info("Elves remaining: " + totalElves.get());
                logger.info("elvesDone: " + elvesDone);
                logger.info("reindeerDone: " + reindeerDone);

                if (elvesDone && reindeerDone) {
                    logger.info("Santa's done for.");
                    break;
                }
            }

            synchronized (santasAvailability) {
                logger.info("Santa's waiting");

                try {
                    santasAvailability.wait();
                } catch (InterruptedException e) {}
                logger.info("Santa's done waiting");
            }

        }
    }

    private void waitForChristmasToEnd(CountDownLatch christmasLatch) {
        try {
            christmasLatch.await();
        } catch (InterruptedException e) {}

        synchronized (christmas) {
            logger.info("Years: " + years.get());
            logger.info("Repetitions: " + repetitions);
            if (years.incrementAndGet() == repetitions) {
                reindeers.forEach(r -> r.yearsOver = true);
            }

            christmasTime = false;
            christmas.notifyAll();
        }

        synchronized (hut) {
            hutLatch = null;
        }
        logger.info("Christmas ended.");
    }

    private void helpElves() {
        logger.info("[Santa Claus] Helping the elves.");
        synchronized (santasShop) {
            santaHelpingElves = true;
            elves.forEach(elf -> elf.waitForMoreElves = false);
            santasShop.notifyAll();
        }

        synchronized (santasShop) {
            while (elvesInNeed.get() != 0) {
                try {
                    santasShop.wait();
                } catch (InterruptedException e) {
                }
            }

            santaHelpingElves = false;
        }
        logger.info("[Santa Claus] Done helping the elves.");
    }

    private void prepareSleigh(CountDownLatch reindeerLatch) {
        logger.info("[Santa Claus] Preparing sleigh.");
        synchronized (hut) {
            reindeers.forEach(r -> r.shouldLeaveHut = true);
            reindeers.forEach(r -> r.christmasLatch = reindeerLatch);
            reindeers.forEach(r -> r.hutLatch.countDown());
        }
    }

    public void reportVacation(Reindeer reindeer) {
        if (reindeers.remove(reindeer)) {
            synchronized (hut) {
                reindeerReady.decrementAndGet();
                reindeer.shouldLeaveHut = false;
            }

            synchronized (christmas) {
                christmas.notifyAll();
            }
        }
    }

    public void leave(Reindeer reindeer) {
        logger.info(String.format("Reindeer %s leaving.", reindeer.name));
        if (reindeers.remove(reindeer)) {
            int numReindeer;
            synchronized (hut) {
                numReindeer = reindeerReady.decrementAndGet();

                synchronized (endOfChristmas) {
                    logger.info("Remaining reindeer: " + numReindeer);
                    if (numReindeer == 0) {
                        reindeerDone = true;
                    }
                }
            }
        }
    }

    public void reindeerReady(Reindeer reindeer) {
        synchronized (hut) {
            if (hutLatch == null) {
                hutLatch = new CountDownLatch(NUM_REINDEER);
            }

            reindeer.hutLatch = hutLatch;
            reindeers.add(reindeer);
            reindeerReady.incrementAndGet();
        }

        synchronized (santasAvailability) {
            santasAvailability.notifyAll();
        }
    }

    public void reportHelpNeeded(Elf elf) {
        synchronized (santasShop) {
            elvesInNeed.incrementAndGet();
            elves.add(elf);
        }
        synchronized (santasAvailability) {
            santasAvailability.notifyAll();
        }
    }

    public boolean isShopFull() {
        return elvesInNeed.get() == 3;
    }

    public void getHelp(Elf elf) {
        logger.info(String.format("[Santa Calus] Helping elf: %s", elf.name));
        if (randomize) {
            try {
                TimeUnit.MILLISECONDS.sleep(new Random().nextInt(500));
            } catch (InterruptedException e) {
            }
        }
        synchronized (santasShop) {
            int inNeed = elvesInNeed.decrementAndGet();
            elves.remove(elf);
            logger.info(String.format("[Santa Calus] Finished helping elf: %s", elf.name));
            logger.info(String.format("[Santa Calus] Now there's %d in line.", inNeed));
            santasShop.notifyAll();
        }
        elfLeaves();
    }

    public void elfLeaves() {
        synchronized (endOfChristmas) {
            logger.info("Elf leaving. Current: " + totalElves.get());
            int current = totalElves.decrementAndGet();
            if (current == 0) {
                elvesDone = true;
            }
        }

        synchronized (santasAvailability) {
            santasAvailability.notifyAll();
        }
    }

    public static void main(String[] args) {
        ExecutableSolution.main(args);

        SantaClaus santaClaus = new SantaClaus();
        Thread santaThread = new Thread(santaClaus);
        santaThread.start();
        for (int i = 0; i < NUM_REINDEER; i++) {
            Thread reindeerThread = new Thread(new Reindeer("Reindeer " + i, santaClaus));
            reindeerThread.start();
        }

        santaClaus.totalElves.set(repetitions * 10);
        for (int i = 1; i <= repetitions * 10; i++) {
            Thread elfThread = new Thread(new Elf("Elf " + i, santaClaus));
            elfThread.start();

            try {
                TimeUnit.MILLISECONDS.sleep(new Random().nextInt(500));
            } catch (InterruptedException e) {}
        }
    }
}
