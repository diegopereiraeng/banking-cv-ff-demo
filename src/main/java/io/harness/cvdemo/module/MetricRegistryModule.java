package io.harness.cvdemo.module;
import com.google.inject.AbstractModule;
import com.codahale.metrics.MetricRegistry;
import io.harness.cvdemo.metrics.CVDemoMetricsRegistry;
import io.prometheus.client.CollectorRegistry;

public class MetricRegistryModule extends AbstractModule {
  private CVDemoMetricsRegistry harnessMetricRegistry;

  private CollectorRegistry collectorRegistry =
      CollectorRegistry.defaultRegistry;

  public MetricRegistryModule(MetricRegistry metricRegistry) {
    harnessMetricRegistry =
        new CVDemoMetricsRegistry(metricRegistry, collectorRegistry);
  }

  @Override
  protected void configure() {
    bind(CVDemoMetricsRegistry.class).toInstance(harnessMetricRegistry);
  }
}
