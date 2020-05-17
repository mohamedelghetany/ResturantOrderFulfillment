package common;

import com.google.gson.Gson;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

/**
 * Data model for an Order
 *
 * Example:
 * {
 * "id": "9012736d-777b-4f5b-a12d-982e302fefa1",
 * "name": "Mixed Greens",
 * "temp": "cold",
 * "shelfLife": 252,
 * "decayRate": 0.26
 * },
 */
public final class Order {
  @Nonnull
  private String id;
  @Nonnull
  private String name;
  @Nonnull
  private Temp temp;
  private int shelfLife;
  private float orderLife;
  private float decayRate;
  private final long createTimeStamp;
  private long ageInSeconds;

  public Order() {
    this.createTimeStamp = System.currentTimeMillis();
  }

  public Order(@Nonnull final String id, @Nonnull final String name, @Nonnull final Temp temp, final int shelfLife, final float decayRate) {
    this.id = id;
    this.name = name;
    this.temp = temp;
    this.shelfLife = shelfLife;
    this.decayRate = decayRate;
    this.orderLife = 0;
    this.createTimeStamp = System.currentTimeMillis();
  }

  /**
   * Encapsulate Deserialization logic, I want to hide that fact that we are using Gson
   *
   * @param jsonOrder to be deserialized
   * @return {@link Order} represents the given input orderJson
   */
  public static Order createFromJson(@Nonnull final String jsonOrder) {
    return new Gson().fromJson(jsonOrder, Order.class);
  }

  public float getDecayRate() {
    return decayRate;
  }

  public int getShelfLife() {
    return shelfLife;
  }

  public Temp getTemp() {
    return temp;
  }

  public String getName() {
    return name;
  }

  public String getId() {
    return id;
  }

  public long getCreateTimeStamp() {
    return createTimeStamp;
  }

  public long getAgeInSeconds() {
    return ageInSeconds;
  }

  public float getOrderLife() {
    return orderLife;
  }

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }

  public float updateAndGetLife() {
    return updateAndGetLife(() -> System.currentTimeMillis());
  }

  public float updateAndGetLife(@Nonnull final Supplier<Long> timeReferenceSupplier) {
    this.ageInSeconds = TimeUnit.MILLISECONDS.toSeconds(timeReferenceSupplier.get() - createTimeStamp);

    orderLife = ((shelfLife - decayRate * ageInSeconds * temp.getDecayModifier()) / shelfLife);

    return orderLife;
  }

  @Override
  public int hashCode() {
    return (this.id + this.name + this.decayRate + this.temp + this.shelfLife).hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null || this.getClass() != obj.getClass()) {
      return false;
    }

    final Order order = (Order) obj;

    return order.id.equals(id) &&
        order.name.equals(name) &&
        order.temp.equals(temp) &&
        order.shelfLife == shelfLife &&
        Float.compare(order.decayRate, decayRate) == 0;
  }
}
