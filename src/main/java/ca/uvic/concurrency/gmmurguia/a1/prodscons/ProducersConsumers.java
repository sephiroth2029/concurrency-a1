// Code adapted from https://www.geeksforgeeks.org/producer-consumer-solution-using-threads-java/

package ca.uvic.concurrency.gmmurguia.a1.prodscons;

import ca.uvic.concurrency.gmmurguia.a1.ExecutableSolution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ProducersConsumers extends ExecutableSolution {

    private static Logger logger = LogManager.getLogger();

    public static void main(String[] args)
    {
        ExecutableSolution.main(args);
        // Object of a class that has both produce()
        // and consume() methods
        final PC pc = new PC();

        // Create producer thread
        Thread t1 = new Thread(() -> {
            try
            {
                pc.produce();
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        });

        // Create consumer thread
        Thread t2 = new Thread(() -> {
            try
            {
                pc.consume();
            }
            catch(InterruptedException e) { }
        });

        // Start both threads
        t1.start();
        t2.start();

        // t1 finishes before t2
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) { }
    }

    // This class has a list, producer (adds items to list
    // and consumber (removes items).
    public static class PC
    {
        // Create a list shared by producer and consumer
        // Size of list is 2.
        LinkedList<Integer> list = new LinkedList<>();
        int capacity = 5;

        // Function called by producer thread
        public void produce() throws InterruptedException
        {
            int value = 0;
            while (value < repetitions)
            {
                synchronized (this)
                {
                    // producer thread waits while list
                    // is full
                    while (list.size()==capacity)
                        wait();

                    logger.info("Producer produced-" + value);

                    // to insert the jobs in the list
                    list.add(value++);

                    // notifies the consumer thread that
                    // now it can start consuming
                    notify();

                    if (randomize) {
                        // makes the working of program easier
                        // to  understand
                        TimeUnit.MILLISECONDS.sleep(new Random().nextInt(1000));
                    }
                }
            }
        }

        // Function called by consumer thread
        public void consume() throws InterruptedException
        {
            int numConsumed = 0;
            while (numConsumed++ < repetitions)
            {
                synchronized (this)
                {
                    // consumer thread waits while list
                    // is empty
                    while (list.size()==0)
                        wait();

                    //to retrive the ifrst job in the list
                    int val = list.removeFirst();

                    logger.info("Consumer consumed-"
                            + val);

                    // Wake up producer thread
                    notify();

                    if (randomize) {
                        // and sleep
                        TimeUnit.MILLISECONDS.sleep(new Random().nextInt(1000));
                    }
                }
            }
        }
    }

}
