package concurrent;

import model.Order;

import java.util.concurrent.LinkedBlockingQueue;

public class OrderQueueManager {
    private static OrderQueueManager instance;
    private final LinkedBlockingQueue<Order> queue;
    private static final int CAPACITY = 50;

    private OrderQueueManager() {
        queue = new LinkedBlockingQueue<>(CAPACITY);
    }

    public static synchronized OrderQueueManager getInstance() {
        if (instance == null) {
            instance = new OrderQueueManager();
        }
        return instance;
    }

    public void enqueue(Order order) {
        try {
            queue.put(order);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Order dequeue() throws InterruptedException {
        return queue.take();
    }

    public int size() {
        return queue.size();
    }
}
