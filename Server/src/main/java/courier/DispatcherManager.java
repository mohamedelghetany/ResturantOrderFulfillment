package courier;

import common.Queue;
import common.ServerProperties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Nonnull;
import org.apache.log4j.Logger;

public class DispatcherManager {
  private final static Logger logger = Logger.getLogger(DispatcherManager.class);
  private final ExecutorService executorService;
  private final Queue queue;

  public DispatcherManager(@Nonnull final Queue queue) {
    this.queue = queue;
    executorService = Executors.newFixedThreadPool(ServerProperties.numOfThreadsForDispatching.get());
  }

  public void initialize(){
    final Integer threadCount = ServerProperties.numOfThreadsForDispatching.get();
    logger.info(String.format("Initializing DispatcherManager with %d order processors", threadCount));

    for (int i = 0; i < threadCount; i++) {
      executorService.submit(new Dispatcher(queue));
    }
  }
}
