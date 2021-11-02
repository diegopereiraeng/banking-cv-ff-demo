package io.harness.cvdemo.config;

import io.harness.cvdemo.App;
import io.harness.cvdemo.config.beans.Config;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ConfigService {

  public Config setConfig(Config config)  {
    log.info("Setting new configuration");
    // TODO - Validate config

    App.behaviorGenerator.applyConfig(config);
    return config;
  }
}
