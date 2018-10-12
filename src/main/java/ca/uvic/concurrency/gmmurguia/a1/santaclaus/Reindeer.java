package ca.uvic.concurrency.gmmurguia.a1.santaclaus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Reindeer implements Runnable {

    private static Logger logger = LogManager.getLogger();

    public CountDownLatch christmasLatch;

    public CountDownLatch hutLatch;

    String name;

    private SantaClaus santaClaus;

    volatile boolean shouldLeaveHut;

    private Object hut;

    private boolean randomize;

    boolean yearsOver;

    public Reindeer(String name, SantaClaus santaClaus) {
        this.name = name;
        this.santaClaus = santaClaus;
        this.hut = santaClaus.hut;
        this.randomize = santaClaus.randomize;
        this.yearsOver = false;
    }

    public void goOnVacation() {
        santaClaus.reportVacation(this);
        logger.info(String.format("%s is on vacation...", name));

        if (randomize) {
            try {
                TimeUnit.MILLISECONDS.sleep(new Random().nextInt(5000));
            } catch (InterruptedException e) {
            }
            logger.info(String.format("%s is back from vacation.", name));
        }
    }

    private void reportToTheNorth() {
        santaClaus.reindeerReady(this);
        waitInHut();
        getHitched();
    }

    private void getHitched() {
        logger.info(String.format("%s is getting hitched.", name));
    }

    public void waitInHut() {
        logger.info(String.format("%s is waiting in the warm hut.", name));
        synchronized (hut) {
            logger.info(String.format("Reindeer ready: %d.", santaClaus.reindeerReady.get()));
        }

        try {
            hutLatch.await();
        } catch (InterruptedException e) {}
    }

    @Override
    public void run() {
        while (true) {
            if (yearsOver) {
                santaClaus.leave(this);
                break;
            }

            goOnVacation();
            reportToTheNorth();

            christmasLatch.countDown();
            waitForChristmasToEnd();

            if (randomize) {
                try {
                    TimeUnit.MILLISECONDS.sleep(new Random().nextInt(500) + 800);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private void waitForChristmasToEnd() {
        synchronized (santaClaus.christmas) {
            try {
                santaClaus.christmas.wait();
            } catch (InterruptedException e) {
            }
        }

        logger.info("All reindeer ready.");
    }
}