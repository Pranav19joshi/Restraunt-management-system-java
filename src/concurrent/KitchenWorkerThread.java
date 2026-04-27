package concurrent;

import enums.OrderStatus;
import interfaces.Notifiable;
import model.Order;
import service.KitchenService;

public class KitchenWorkerThread implements Runnable {
    private final KitchenService kitchenService;
    private final Notifiable notifiable;
    private volatile boolean running = true;

    public KitchenWorkerThread(KitchenService kitchenService, Notifiable notifiable) {
        this.kitchenService = kitchenService;
        this.notifiable = notifiable;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        OrderQueueManager queue = OrderQueueManager.getInstance();
        while (running) {
            try {
                Order order = queue.dequeue();
                processOrder(order);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void processOrder(Order order) throws InterruptedException {
        // PENDING -> RECEIVED
        Thread.sleep(500);
        kitchenService.setOrderStatus(order.getOrderId(), OrderStatus.RECEIVED);

        // RECEIVED -> PREPARING
        kitchenService.setOrderStatus(order.getOrderId(), OrderStatus.PREPARING);

        // PREPARING -> READY (2 seconds per item)
        int itemCount = order.getItems().size();
        Thread.sleep(itemCount * 2000L);
        kitchenService.setOrderStatus(order.getOrderId(), OrderStatus.READY);

        // Notify UI
        notifiable.onOrderReady(order.getOrderId());
    }
}
