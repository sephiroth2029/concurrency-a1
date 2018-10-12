package ca.uvic.concurrency.gmmurguia.a1.readerswriters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Reader {

    private static Logger logger = LogManager.getLogger();

    private static volatile int numReaders = 0;
    private static volatile boolean reading = false;
    private final boolean randomize;

    public Reader(boolean randomize) {
        this.randomize = randomize;
    }

    public void read(Object roomLock) throws InterruptedException {
        if (randomize) {
            TimeUnit.MILLISECONDS.sleep(new Random().nextInt(100));
        }
        synchronized (roomLock) {
            if (++numReaders == 1) {
                logger.info("First reader, closing the room.");
                reading = true;
            }
        }

        logger.info("Reading...");
        if (randomize) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) { }
        }

        synchronized (roomLock) {
            if (--numReaders == 0) {
                logger.info("Last reader, opening the room.");
                reading = false;
                roomLock.notifyAll();
            }
        }
    }

    public static boolean isReading() {
        return reading;
    }
}
