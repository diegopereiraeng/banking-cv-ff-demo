package io.harness.cvdemo;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import io.harness.cvdemo.payments.PaymentsResource;
import io.harness.cvdemo.behavior.BehaviorGenerator;
import io.harness.cvdemo.config.ConfigResource;
import io.harness.cvdemo.config.beans.Config;
import io.harness.cvdemo.metrics.CVDemoMetricsRegistry;
import io.harness.cvdemo.metrics.MetricResource;
import io.harness.cvdemo.metrics.PrometheusMetricResource;
import io.harness.cvdemo.module.MetricRegistryModule;
import io.prometheus.client.CollectorRegistry;

import org.eclipse.jetty.servlets.CrossOriginFilter;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

public class App extends Application<AppConfiguration> {

  public static final BehaviorGenerator behaviorGenerator =
      new BehaviorGenerator();
  public static Config defaultConfig;

  private final MetricRegistry metricRegistry = new MetricRegistry();

  @Override
  public void run(AppConfiguration c, Environment e)  {
    List<Module> moduleList = new ArrayList<>();
    moduleList.add(new MetricRegistryModule(metricRegistry));
    Injector injector = Guice.createInjector(moduleList);

    e.jersey().register(new CVDemoMetricsRegistry(
        new MetricRegistry(), CollectorRegistry.defaultRegistry));


    e.jersey().register(injector.getInstance(PrometheusMetricResource.class));
    e.jersey().register(injector.getInstance(ConfigResource.class));
    e.jersey().register(injector.getInstance(MetricResource.class));
    e.jersey().register(injector.getInstance(PaymentsResource.class));
    // Enable CORS headers
    final FilterRegistration.Dynamic cors =
            e.servlets().addFilter("CORS", CrossOriginFilter.class);

    // Configure CORS parameters
    cors.setInitParameter("allowedOrigins", "*");
    cors.setInitParameter("allowedHeaders",
            "Cache-Control,If-Modified-Since,Pragma,Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
    cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

    // Add URL mapping
    cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");

    // DO NOT pass a preflight request to down-stream auth filters
    // unauthenticated preflight requests should be permitted by spec
    cors.setInitParameter(CrossOriginFilter.CHAIN_PREFLIGHT_PARAM, Boolean.FALSE.toString());
    //registerCorsFilter(c.getAllowedOrigins(), e);

    defaultConfig = c.getDefaultConfig();
    behaviorGenerator.init(c);
  }

  private void registerCorsFilter(String allowedOrigins,
                                  Environment environment) {
    FilterRegistration.Dynamic cors =
        environment.servlets().addFilter("CORS", CrossOriginFilter.class);
    cors.setInitParameters(ImmutableMap.of(
            "allowedOrigins", allowedOrigins, "allowedHeaders",
            "X-Requested-With,Content-Type,Accept,Origin,Authorization,X-api-key",
        "allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD", "preflightMaxAge",
        "86400"));
    cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    cors.setInitParameter("allowedOrigins", "*");
  }

  public static void main(String[] args) throws Exception {
    new App().run(args);
  }
}
