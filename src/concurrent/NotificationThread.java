package concurrent;

import interfaces.Notifiable;

import java.util.concurrent.LinkedBlockingQueue;

public class NotificationThread implements Runnable, Notifiable {

    public interface NotificationListener {
        void onNotification(String message, boolean isReady);
    }

    private static class NotificationEvent {
        final String message;
        final boolean isReady;

        NotificationEvent(String message, boolean isReady) {
            this.message = message;
            this.isReady = isReady;
        }
    }

    private final LinkedBlockingQueue<NotificationEvent> eventQueue = new LinkedBlockingQueue<>();
    private NotificationListener listener;
    private volatile boolean running = true;

    public void setListener(NotificationListener listener) {
        this.listener = listener;
    }

    public void stop() {
        running = false;
    }

    @Override
    public synchronized void onOrderReady(String orderId, int tableNumber) {
        String message = "Order " + orderId + " is READY for Table " + tableNumber + "!";
        eventQueue.offer(new NotificationEvent(message, true));
    }

    @Override
    public synchronized void onOrderCancelled(String orderId) {
        String message = "Order " + orderId + " has been CANCELLED.";
        eventQueue.offer(new NotificationEvent(message, false));
    }

    @Override
    public void run() {
        while (running) {
            try {
                NotificationEvent event = eventQueue.take();
                processEvent(event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void processEvent(NotificationEvent event) {
        if (listener != null) {
            listener.onNotification(event.message, event.isReady);
        }
    }
}
