package common;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import javax.annotation.Nonnull;

/**
 * {@link Queue} implementation.
 *
 * This implementation Blocks on fetch.
 *
 */
public class OrdersQueue implements Queue {
  private final BlockingDeque<Order> deque;

  public OrdersQueue() {
    this.deque = new LinkedBlockingDeque<>();
  }

  @Override
  public Order fetch() throws InterruptedException {
    return deque.take();
  }

  @Override
  public void add(@Nonnull Order order) {
    deque.offer(order);
  }
}
