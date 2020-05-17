package common;

import javax.annotation.Nonnull;

/**
 * Simple interface for processing an Order
 */
public interface Processor {
  /**
   * @param order to be processed
   * @return true if processing an order has succeeded, false otherwise
   */
  boolean process(@Nonnull final Order order);
}
