package processor;

import common.Queue;
import common.ServerProperties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Nonnull;
import org.apache.log4j.Logger;

public class OrderProcessorManager {
  private final static Logger logger = Logger.getLogger(OrderProcessorManager.class);
  private final ExecutorService executorService;
  private final Queue queue;

  public OrderProcessorManager(@Nonnull final Queue queue) {
    this.queue = queue;
    executorService = Executors.newFixedThreadPool(ServerProperties.numOfThreadsForOrderProcessing.get());
  }

  public void initialize(){
    logger.info(String.format("Initializing OrderProcessorManager with %d order processors", ServerProperties.numOfThreadsForOrderProcessing.get()));

    for (int i = 0; i < ServerProperties.numOfThreadsForOrderProcessing.get(); i++) {
      executorService.submit(new OrderProcessor(queue));
    }
  }
}
