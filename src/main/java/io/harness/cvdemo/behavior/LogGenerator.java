package io.harness.cvdemo.behavior;


import io.harness.cvdemo.App;
import io.harness.cvdemo.config.ConfigService;
import io.harness.cvdemo.config.beans.LogConfig;
import io.harness.cvdemo.config.beans.ElkLogPublishConfig;

import io.harness.cf.client.api.CfClient;
import io.harness.cf.client.api.Config;
import io.harness.cf.client.dto.Target;

import java.security.SecureRandom;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response;

import static com.google.common.base.Strings.nullToEmpty;

@Slf4j
public class LogGenerator implements Runnable {
  private final LogConfig logConfig;
  private final ElkLogPublishConfig elkLogPublishConfig;
  private final LogPublisher logPublisher;
  private String ffName = "";
  private SecureRandom r = new SecureRandom();

  public LogGenerator(LogConfig logConfig, ElkLogPublishConfig elkLogPublishConfig, String name) {
    this.logConfig = logConfig;
    this.elkLogPublishConfig = elkLogPublishConfig;
    this.ffName = name;
    logPublisher = new LogPublisher(elkLogPublishConfig);
  }


  @SneakyThrows
  @Override
  public void run() {

    log.info("FF log start");

    /**
     * Define you target on which you would like to evaluate the featureFlag
     */
    if ( this.ffName.equals("")){
      this.ffName = "ffcvdemo2";
    }

    Target target = Target.builder()
            .name(elkLogPublishConfig.getTarget())
            .identifier(elkLogPublishConfig.getTarget())
            .build();

    for (int i = 0; i < logConfig.getLogsPerMinute(); i++) {
      String logMsg = "", level = "ERROR";

      log.info("FF config name: "+this.ffName);

      try{



        //io.harness.cvdemo.config.beans.Config config2 = App.behaviorGenerator.getConfig();
        //config2.setDarkTheme(resultDarktheme);
        //App.behaviorGenerator.applyConfig(config2);
        boolean result =
                App.behaviorGenerator.checkFlag(elkLogPublishConfig.getFfLogKey());
        log.info("FF Log Boolean variation for target" + elkLogPublishConfig.getTarget()+ " is " + result );

        if (Math.round(r.nextFloat() * 100) < logConfig.getErrorRate()) {
          logMsg = String.format("error-%d %s", i, nullToEmpty(logConfig.getErrorMessage()));
          log.error(logMsg);
        } else {
          logMsg = String.format("info-%d", i);
          log.info(logMsg);
          level = "INFO";
        }
        if (result) {
          String[] listErrors = new String[] { "Feature Flags Error Exception", "FF Bad Things Exception",
                  "Login Exception FF", "Unexpected character % - FF", "Feature Flags is awesome Exception" };
          String randomError = listErrors[r.nextInt(listErrors.length)];;

          String logError = "Feature Flags Error Exception";
          log.info("FF "+elkLogPublishConfig.getFfLogKey()+" activated - message: "+randomError);
          logMsg = String.format("error-%d %s", i, nullToEmpty(randomError));
          log.error(logMsg);
        }
        else{
          log.info("FF "+elkLogPublishConfig.getFfLogKey()+" not activated - ff status: "+result);
        }
        logPublisher.publishLogs(level, logMsg);
        Thread.sleep(60000 / logConfig.getLogsPerMinute());
      }
      catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      } catch (Exception e) {
        log.error(e.getMessage());
      }





    }
  }
}
