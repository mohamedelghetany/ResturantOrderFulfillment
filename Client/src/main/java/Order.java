import com.google.gson.Gson;

/**
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
  private String temp;
  private int shelfLife;
  private final Gson gson;

  public Order(String id, String name, String temp, int shelfLife, float decayRate) {
    this.id = id;
    this.name = name;
    this.temp = temp;
    this.shelfLife = shelfLife;
    this.decayRate = decayRate;

    gson = new Gson();
  }

  private float decayRate;

  public float getDecayRate() {
    return decayRate;
  }

  public int getShelfLife() {
    return shelfLife;
  }

  public String getTemp() {
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
}
