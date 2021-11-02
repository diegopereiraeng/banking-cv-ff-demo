package io.harness.cvdemo;

import io.dropwizard.Configuration;
import io.harness.cvdemo.config.beans.Config;
import io.harness.cvdemo.config.beans.ElkLogPublishConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AppConfiguration extends Configuration {
  private String allowedOrigins;
  private Config defaultConfig;
  private String elkUrl;
  private String elkIndex;
  private String ffApiKey;
  private String ffMetricKey;
  private String ffLogKey;
  private String target;
}
