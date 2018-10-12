package ca.uvic.concurrency.gmmurguia.a1.readerswriters;

import ca.uvic.concurrency.gmmurguia.a1.ExecutableSolution;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ReadersWriters extends ExecutableSolution {

    private static volatile Object roomLock = new Object();

    public static void main(String[] args) {
        ExecutableSolution.main(args);

        doRun();
    }

    private static void doRun() {
        for (int i = 0; i < repetitions; i++) {
            Thread reader = new Thread(() -> {
                try {
                    new Reader(randomize).read(roomLock);
                } catch (InterruptedException e) {}
            });

            Thread writer = new Thread(() -> {
                try {
                    new Writer(randomize).write(roomLock);
                } catch (InterruptedException e) {}
            });

            if (!randomize || new Random().nextBoolean()) {
                reader.start();
            }

            if (!randomize || new Random().nextBoolean()) {
                writer.start();
            }
        }
    }
}
