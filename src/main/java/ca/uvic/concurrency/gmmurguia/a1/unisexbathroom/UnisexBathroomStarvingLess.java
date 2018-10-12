package ca.uvic.concurrency.gmmurguia.a1.unisexbathroom;

import ca.uvic.concurrency.gmmurguia.a1.ExecutableSolution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class UnisexBathroomStarvingLess extends ExecutableSolution {

    private static Logger logger = LogManager.getLogger();

    private static volatile int numMales = 0;
    private static volatile int numFemales = 0;
    private static volatile int runningFemales = 0;
    private static volatile int runningMales = 0;

    private static volatile Object bathroomLock = new Object();

    public void enterFemale() {
        boolean canProceed = false;
        do {
            synchronized (bathroomLock) {
                if (numMales == 0 && numFemales < 3 && runningFemales < 5) {
                    canProceed = true;
                    numFemales++;
                    runningFemales++;
                    logger.info("Female entered the bathroom.");
                    logger.info(String.format("Totals: Female[%d], Male[%d]", numFemales, numMales));
                }
            }
        } while (!canProceed);

        if (randomize) {
            try {
                TimeUnit.MILLISECONDS.sleep(new Random().nextInt(1000));
            } catch (InterruptedException e) {
            }
        }

        synchronized (bathroomLock) {
            numFemales--;
            logger.info("Female left the bathroom.");
            logger.info(String.format("Totals: Female[%d], Male[%d]", numFemales, numMales));

            if (numFemales == 0) {
                runningFemales = 0;
            }
        }
    }

    public void enterMale() {
        boolean canProceed = false;
        do {
            synchronized (bathroomLock) {
                if (numFemales == 0 && numMales < 3 && runningMales < 5) {
                    canProceed = true;
                    numMales++;
                    runningMales++;
                    logger.info("Male entered the bathroom.");
                    logger.info(String.format("Totals: Female[%d], Male[%d]", numFemales, numMales));
                }
            }
        } while (!canProceed);

        if (randomize) {
            try {
                TimeUnit.MILLISECONDS.sleep(new Random().nextInt(1000));
            } catch (InterruptedException e) {
            }
        }

        synchronized (bathroomLock) {
            numMales--;
            logger.info("Male left the bathroom.");
            logger.info(String.format("Totals: Female[%d], Male[%d]", numFemales, numMales));

            if (numMales == 0) {
                runningMales = 0;
            }
        }
    }

    public static void main(String[] args) {
        ExecutableSolution.main(args);
        ExecutorService executor = Executors.newFixedThreadPool(100);
        Random r = new Random(948);
        UnisexBathroomStarvingLess unisexBathroom = new UnisexBathroomStarvingLess();

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
