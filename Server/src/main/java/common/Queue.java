package common;

import javax.annotation.Nonnull;

public interface Queue {
  /**
   * Fetch an order form the queue
   *
   * Fetch might Block and wait for a message, depends on the concrete implementation
   */
  Order fetch() throws InterruptedException;

  /**
   * add message to the queue
   *
   * @param order to be added
   * @return true if the given Order has been added to the queue, false otherwise
   */
  boolean add(@Nonnull final Order order);
}
