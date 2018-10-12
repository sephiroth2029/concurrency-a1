package ca.uvic.concurrency.gmmurguia.a1.unisexbathroom;

import ca.uvic.concurrency.gmmurguia.a1.ExecutableSolution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class UnisexBathroom extends ExecutableSolution {

    private static Logger logger = LogManager.getLogger();

    private static volatile int numMales = 0;
    private static volatile int numFemales = 0;
    private static volatile int runningFemales = 0;
    private static volatile int runningMales = 0;
    private static volatile AtomicInteger waitingMales = new AtomicInteger(0);
    private static volatile AtomicInteger waitingFemales = new AtomicInteger(0);


    private static volatile Object bathroomLock = new Object();

    public void enterFemale() {
        boolean canProceed = false;
        waitingFemales.incrementAndGet();
        do {
            synchronized (bathroomLock) {
                if (numMales == 0 && numFemales < 3 && (runningFemales < 5 || waitingMales.get() == 0)) {
                    canProceed = true;
                    numFemales++;
                    runningFemales++;
                    logger.info("Female entered the bathroom.");
                    logger.info(String.format("Totals: Female[%d], Male[%d], Waiting [%d, %d]",
                            numFemales, numMales, waitingFemales.get(), waitingMales.get()));
                }
            }
        } while (!canProceed);

        waitingFemales.decrementAndGet();

        if (randomize) {
            try {
                TimeUnit.MILLISECONDS.sleep(new Random().nextInt(1000));
            } catch (InterruptedException e) {
            }
        }

        synchronized (bathroomLock) {
            runningMales = 0;
            numFemales--;
            logger.info("Female left the bathroom.");
            logger.info(String.format("Totals: Female[%d], Male[%d], Waiting [%d, %d]",
                    numFemales, numMales, waitingFemales.get(), waitingMales.get()));
        }
    }

    public void enterMale() {
        boolean canProceed = false;
        waitingMales.incrementAndGet();
        do {
            synchronized (bathroomLock) {
                if (numFemales == 0 && numMales < 3 && (runningMales < 5 || waitingFemales.get() == 0)) {
                    canProceed = true;
                    numMales++;
                    runningMales++;
                    logger.info("Male entered the bathroom.");
                    logger.info(String.format("Totals: Female[%d], Male[%d], Waiting [%d, %d]",
                            numFemales, numMales, waitingFemales.get(), waitingMales.get()));
                }
            }
        } while (!canProceed);

        waitingMales.decrementAndGet();

        if (randomize) {
            try {
                TimeUnit.MILLISECONDS.sleep(new Random().nextInt(1000));
            } catch (InterruptedException e) {
            }
        }

        synchronized (bathroomLock) {
            runningFemales = 0;
            numMales--;
            logger.info("Male left the bathroom.");
            logger.info(String.format("Totals: Female[%d], Male[%d], Waiting [%d, %d]",
                    numFemales, numMales, waitingFemales.get(), waitingMales.get()));
        }
    }

    public static void main(String[] args) {
        ExecutableSolution.main(args);
        ExecutorService executor = Executors.newFixedThreadPool(100);
        Random r = new Random(948);
        UnisexBathroom unisexBathroom = new UnisexBathroom();

        for (int i = 0; i < repetitions; i++) {
            if ((!randomize && i % 2 == 0) || (randomize && r.nextBoolean())) {
                executor.execute(() -> unisexBathroom.enterFemale());
            } else {
                executor.execute(() -> unisexBathroom.enterMale());
            }
        }

        executor.shutdown();
    }

}
