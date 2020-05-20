package common;

import javax.annotation.Nonnull;

/**
 * General exception for our sever, all other specific exceptions should extend this class
 */
public class RestaurantException extends Exception {
  public RestaurantException(@Nonnull final String message, @Nonnull final Exception e) {
    super(message, e);
  }
}
