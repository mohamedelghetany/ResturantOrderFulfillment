package common;

import com.google.gson.annotations.SerializedName;

public enum Temp {
  @SerializedName(value = "hot", alternate = "HOT")
  HOT("Hot shelf", 1, 10),
  @SerializedName(value = "cold", alternate = "COLD")
  COLD("Cold shelf", 1, 10),
  @SerializedName(value = "frozen", alternate = "FROZEN")
  FROZEN("Frozen shelf", 1, 10),
  @SerializedName(value = "any", alternate = "ANY")
  ANY("Overflow shelf", 2, 15);

  private final String shelfName;
  private final int decayModifier;
  private final int capacity;

  Temp(final String shelfName, final int decayModifier, final int capacity) {
    this.shelfName = shelfName;
    this.decayModifier = decayModifier;
    this.capacity = capacity;
  }

  public int getDecayModifier() {
    return decayModifier;
  }

  public int getCapacity() {
    return capacity;
  }

  public String getShelfName() {
    return shelfName;
  }
}
