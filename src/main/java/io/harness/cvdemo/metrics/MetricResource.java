package io.harness.cvdemo.metrics;

import static io.harness.cvdemo.metrics.Constants.DELAY_CALL;
import static io.harness.cvdemo.metrics.Constants.ERROR_CALL;
import static io.harness.cvdemo.metrics.Constants.NORMAL_CALL;

import com.google.inject.Inject;

import java.security.SecureRandom;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("metric")
@Produces(MediaType.APPLICATION_JSON)
public class MetricResource {
  @Inject private CVDemoMetricsRegistry metricRegistry;
  private SecureRandom r = new SecureRandom();

  @GET
  @Path("normal-call")
  public Response executeNormalCall() {
    metricRegistry.recordGaugeValue(NORMAL_CALL, null, 1);
    return Response.ok().build();
  }

  @GET
  @Path("error-call")
  public Response executeErrorCall(@QueryParam("value") double value) {

    metricRegistry.recordGaugeValue(ERROR_CALL, null, Math.abs(value));
    return Response.serverError().build();
  }

  @GET
  @Path("delayed-call")
  public Response executeDelayedCall() {
    int max = 900, min = 400;
    int msDelay = r.nextInt((max - min) + 1) + min;
    try {
      Thread.sleep(msDelay);
      metricRegistry.recordGaugeValue(DELAY_CALL, null, 1);
      return Response.ok().build();
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      return Response.serverError().build();
    }
    return Response.serverError().build();

  }
}
