package common;

import com.google.gson.Gson;
import javax.annotation.Nonnull;

/**
 * Data model for an common.Order
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
public class Order {
  private String id;
  private String name;
  private Temp temp;
  private int shelfLife;
  private int orderLife;
  private float decayRate;
  private final long createTimeStamp;

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

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }

  public int updateAndGetLife() {
    orderLife = (int) ((shelfLife - decayRate * (System.currentTimeMillis() - createTimeStamp) * temp.getDecayModifier()) / shelfLife);

    return orderLife;
  }


}
