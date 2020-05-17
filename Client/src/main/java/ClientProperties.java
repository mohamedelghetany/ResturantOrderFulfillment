import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * Encapsulates all properties needed to configure the client.
 *
 * Each property has a default value but it can be overriden by
 * adding the property key in config.properties file
 *
 * The keys in config.properties takes precedence over the default value.
 *
 * Also, if this class couldn't load config.properties file, it will not crash or quit
 * the client. It will continue with using the default value and log a warning message.
 */
public final class ClientProperties {
  private static final Logger logger = Logger.getLogger(ClientProperties.class);

  private static Properties propsFromFile = new Properties();

  private ClientProperties() {
  }

  public static void initialize() {
    try {
      logger.debug("Initializing ClientProperties...");
      final FileInputStream in = new FileInputStream(ClientProperties.class.getClassLoader().getResource("").getPath() + "config.properties");
      propsFromFile.load(in);
      in.close();
    } catch (IOException e) {
      // Handling the case when the intention is to use the default properties and the file doesn't exist
      logger.warn("Could not load config.properties file. Defaulting to the default values");
    }
  }

  public static PropertyKey<Integer> RATE = new IntegerPropertyKey("rate", 2000);

  public static abstract class PropertyKey<T> {
    private final String key;
    private final T defaultValue;

    public PropertyKey(String key, T defaultValue) {
      this.key = key;
      this.defaultValue = defaultValue;
    }

    public String getKey() {
      return key;
    }

    protected T getDefaultValue() {
      return defaultValue;
    }

    public abstract T get();
  }

  public static final class IntegerPropertyKey extends PropertyKey<Integer> {

    public IntegerPropertyKey(String key, Integer defaultValue) {
      super(key, defaultValue);
    }

    @Override
    public Integer get() {
      final String value = propsFromFile.getProperty(getKey());

      if (value == null) {
        return getDefaultValue();
      }

      try {
        return Integer.valueOf(value);
      }catch (final NumberFormatException e) {
        logger.error(String.format("Could not load Property. Defaulting to the default value. PropertyName: %s, InputValue: %s", getKey(), value));
        return getDefaultValue();
      }
    }
  }
}
