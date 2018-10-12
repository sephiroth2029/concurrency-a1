// Code adapted from https://codereview.stackexchange.com/questions/224/thread-safe-and-lock-free-queue-implementation

package ca.uvic.concurrency.gmmurguia.a1.concurrentqueue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

public class LockFreeQueue<T> implements Queue<T> {

    private static class Node<E> {
        E value;
        volatile Node<E> next;

        Node(E value) {
            this.value = value;
        }
    }

    private final AtomicReference<Node<T>> refHead, refTail;
    public LockFreeQueue() {
        // have both head and tail point to a dummy node
        Node<T> dummy = new Node<>(null);
        refHead = new AtomicReference<>(dummy);
        refTail = new AtomicReference<>(dummy);
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        final AtomicReference<Node<T>> refHead = this.refHead;
        return new Iterator<T>() {

            private AtomicReference<Node<T>> currentHead = refHead;

            @Override
            public boolean hasNext() {
                return currentHead.get().next != null;
            }

            @Override
            public T next() {
                Node<T> head, next;

                do {
                    head = currentHead.get();
                    next = head.next;
                    if (next == null) {
                        // empty list
                        return null;
                    }
                    // try until the whole loop executes pseudo-atomically
                    // (i.e. unaffected by modifications done by other threads)
                } while (!currentHead.compareAndSet(head, next));

                T value = next.value;

                // release the value pointed to by head, keeping the head node dummy
                next.value = null;


                return value;
            }
        };
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return null;
    }

    /**
     * Puts an object at the end of the queue.
     */
    public boolean add(T value) {
        if (value == null) throw new NullPointerException();

        Node<T> node = new Node<>(value);
        Node<T> prevTail = refTail.getAndSet(node);
        prevTail.next = node;

        return true;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean offer(T t) {
        return false;
    }

    /**
     * Gets an object from the beginning of the queue. The object is removed
     * from the queue. If there are no objects in the queue, returns null.
     */
    public T remove() {
        Node<T> head, next;

        // move head node to the next node using atomic semantics
        // as long as next node is not null
        do {
            head = refHead.get();
            next = head.next;
            if (next == null) {
                // empty list
                return null;
            }
            // try until the whole loop executes pseudo-atomically
            // (i.e. unaffected by modifications done by other threads)
        } while (!refHead.compareAndSet(head, next));

        T value = next.value;

        // release the value pointed to by head, keeping the head node dummy
        next.value = null;

        return value;
    }

    @Override
    public T poll() {
        return null;
    }

    @Override
    public T element() {
        return null;
    }

    @Override
    public T peek() {
        return null;
    }

}
