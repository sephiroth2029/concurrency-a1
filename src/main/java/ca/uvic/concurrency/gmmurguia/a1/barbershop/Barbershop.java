package ca.uvic.concurrency.gmmurguia.a1.barbershop;

import ca.uvic.concurrency.gmmurguia.a1.ExecutableSolution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Barbershop extends ExecutableSolution {

    private static Logger logger = LogManager.getLogger();

    private Barber barber = new Barber();
    private Object lock = new Object();
    private volatile int customersInShop = 0;
    public static final int seatsInBarbershop = 5;

    private class Barber {

        private Object lock = new Object();

        public void doHairCut() {
            synchronized (lock) {
                logger.info("[Barber] Doing haircut...");
                if (randomize) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(new Random().nextInt(2000));
                    } catch (InterruptedException e) {
                    }
                }
                logger.info("[Barber] Finished haircut...");
            }
        }

    }

    private static class Customer implements Runnable {

        private Barbershop barbershop;

        public Customer(Barbershop barbershop) {
            this.barbershop = barbershop;
        }

        @Override
        public void run() {
            logger.info(String.format("[Customer] Entering for a haircut. (%s)", Thread.currentThread().getName()));
            if (!barbershop.getHairCut()) {
                logger.info(String.format("[Customer] Full barbershop,  leaving. (%s)", Thread.currentThread().getName()));
            }
        }
    }

    private boolean getHairCut() {
        synchronized (lock) {
            if (customersInShop - 1 >= seatsInBarbershop) {
                return false;
            }
            customersInShop++;
            logger.info("Customers in barbershop: " + customersInShop);
        }

        barber.doHairCut();

        synchronized (lock) {
            customersInShop--;
            logger.info("Customers in barbershop: " + customersInShop);
        }

        return true;
    }

    public static void main(String[] args) {
        ExecutableSolution.main(args);
        Barbershop barbershop = new Barbershop();

        for (int i = 0; i < repetitions; i++) {
            Customer c = new Customer(barbershop);
            Thread t = new Thread(c);
            t.start();

            if (randomize) {
                try {
                    TimeUnit.MILLISECONDS.sleep(new Random().nextInt(1000));
                } catch (InterruptedException e) {
                }
            }
        }
    }

}
