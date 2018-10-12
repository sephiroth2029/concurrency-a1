package ca.uvic.concurrency.gmmurguia.a1.concurrentqueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class QueueTest {

    private static Logger logger = LogManager.getLogger();

    private static int totalOperations = 10;

    public static void main(String[] args) {

        totalOperations = Integer.valueOf(args[1]);

        if ("lock-free".equals(args[0])) {
            final LockFreeQueue<Integer> lockFreeQueue = new LockFreeQueue<>();
            async(lockFreeQueue);
        } else if ("concurrent".equals(args[0])) {
            final ConcurrentLinkedQueue<Integer> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();
            async(concurrentLinkedQueue);
        }  else if ("blocking".equals(args[0])) {
            final LinkedBlockingQueue<Integer> linkedBlockingQueue = new LinkedBlockingQueue<>();
            async(linkedBlockingQueue);
        }  else if ("sync".equals(args[0])) {
            sync();
        } else {
            throw new IllegalArgumentException("Invalid usage.");
        }
    }

    private static void async(Queue<Integer> queue) {
        Random r = new Random(666);
        Runnable add = () -> queue.add(r.nextInt());
        Runnable remove = () -> queue.remove();
        Runnable iterate = () -> queue.forEach(i -> logger.info(i));

        ExecutorService executor = Executors.newFixedThreadPool(100);

        for (int i = 0; i < totalOperations; i++) {
            executor.execute(add);
        }

        for (int i = 0; i < totalOperations; i++) {
            executor.execute(remove);
            executor.execute(iterate);
        }
        executor.shutdown();
    }

    private static void sync() {
        logger.info("Executing sync");
        Random r = new Random(666);
        final Queue<Integer> queue = new LinkedList<>();

        for (int i = 0; i < totalOperations; i++) {
            queue.add(r.nextInt());
        }

        for (int i = 0; i < totalOperations; i++) {
            queue.remove();
        }
    }
}
