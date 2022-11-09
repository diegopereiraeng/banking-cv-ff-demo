package io.harness.cvdemo.behavior;

import io.harness.cf.client.api.CfClient;
import io.harness.cf.client.dto.Target;
import io.harness.cvdemo.AppConfiguration;
import io.harness.cvdemo.config.beans.Config;
import io.harness.cvdemo.config.beans.ElkLogPublishConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BehaviorGenerator {
  private Config config;
  private ElkLogPublishConfig elkLogPublishConfig;
  private ScheduledExecutorService executorService;
  private List<ScheduledFuture> running;
  private Target target;

  private CfClient cfClient;

  public boolean checkFlag(String flag) {
    boolean result = cfClient.boolVariation(flag, target, false);

    return result;
  }

  public void init(AppConfiguration config)  {
    executorService = new ScheduledThreadPoolExecutor(100);
    running = new ArrayList<>();
    applyConfig(config);
    String ffKey = System.getenv("FF_KEY");
    cfClient = new CfClient(ffKey, io.harness.cf.client.api.Config.builder().build());
    target = Target.builder().name("BehaviorGenerator").identifier("diego.pereira@harness.io").build();

  }

  public void startAll()  {
    running.add(executorService.scheduleAtFixedRate(
        new LogGenerator(config.getLogConfig(), this.elkLogPublishConfig, config.getName()), 0, 1, TimeUnit.MINUTES));
    running.add(executorService.scheduleAtFixedRate(
        new MetricsGenerator(config.getMetricConfig(), this.elkLogPublishConfig ), 0, 1,
        TimeUnit.MINUTES));
    config.setRunning(true);
  }

  public void stopAll() {
    running.forEach(future -> future.cancel(true));
    running.clear();
    config.setRunning(false);
  }
  public Config getConfig() {

    boolean resultFlag1;
    String resultFlag2;
    resultFlag1 =
            cfClient.boolVariation("external_transaction", target, false);
    log.info("FF external_transaction Boolean variation for target" + target+ " is " + resultFlag1 );

//    resultFlag2 =
//            cfClient.stringVariation("transaction_url", target, "default_value");
//    log.info("FF transaction_url Boolean variation for target" + target+ " is " + resultFlag2 );

    config.setDarkTheme(resultFlag1);

    log.info("FF Dark_Theme set to "+config.getDarkTheme());

    return config;
  }

  public void applyConfig(AppConfiguration config)  {
    this.elkLogPublishConfig = ElkLogPublishConfig.builder()
            .elkUrl(config.getElkUrl())
            .elkIndex(config.getElkIndex())
            .ffApiKey(config.getFfApiKey())
            .ffLogKey(config.getFfLogKey())
            .ffMetricKey(config.getFfMetricKey())
            .target(config.getTarget())
            .build();
    applyConfig(config.getDefaultConfig());
    log.info("New configuration applied");
  }

  public void applyConfig(Config config)  {
    this.config = config;
    boolean running = config.isRunning();
    stopAll();
    if (running) {
      startAll();
    }
    log.info("New configuration applied");
  }
}
