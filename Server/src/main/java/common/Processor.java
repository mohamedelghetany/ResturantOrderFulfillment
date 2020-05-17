package common;

import javax.annotation.Nonnull;

public interface Processor {

  boolean process(@Nonnull final Order order);
}
