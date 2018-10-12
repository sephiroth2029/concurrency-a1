package ca.uvic.concurrency.gmmurguia.a1.readerswriters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Writer {

    private static Logger logger = LogManager.getLogger();

    private boolean randomize;

    public Writer(boolean randomize) {
        this.randomize = randomize;
    }

    public void write(Object roomLock) throws InterruptedException {
        if (randomize) {
            TimeUnit.MILLISECONDS.sleep(new Random().nextInt(2000));
        }
        synchronized (roomLock) {
            if (Reader.isReading()) {
                roomLock.wait();
            }

            logger.info("Writing...");
        }
    }

}
