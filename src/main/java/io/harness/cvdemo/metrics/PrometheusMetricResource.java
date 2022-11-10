package io.harness.cvdemo.metrics;

import static io.harness.cvdemo.metrics.Constants.*;
import static io.harness.cvdemo.payments.Constants.LIST;
import static io.harness.cvdemo.payments.Constants.PROCESS;

import com.google.common.collect.Sets;
import com.google.inject.Inject;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import io.harness.cvdemo.config.ConfigService;
import io.prometheus.client.exporter.common.TextFormat;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("metrics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PrometheusMetricResource {
  public static final Set<String> METRICS =
      Sets.newHashSet(NORMAL_CALL, ERROR_CALL, DELAY_CALL, Constants.LIST, STATUS, Constants.PROCESS, LIST_RT, STATUS_RT, PROCESS_RT);

  @Inject private CVDemoMetricsRegistry metricRegistry;

  @GET
  @Timed
  @ExceptionMetered
  public String get() throws IOException {
    final StringWriter writer = new StringWriter();
    Set<String> metrics = new HashSet<>(METRICS);
    try {
      TextFormat.write004(writer, metricRegistry.getMetric(metrics));
      writer.flush();
    } finally {
      writer.close();
    }
    metricRegistry.resetValues(METRICS);
    return writer.getBuffer().toString();
  }
}
