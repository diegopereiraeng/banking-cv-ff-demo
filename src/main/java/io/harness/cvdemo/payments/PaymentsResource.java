package io.harness.cvdemo.payments;

import com.google.inject.Inject;
import io.harness.cf.client.api.CfClient;
import io.harness.cf.client.dto.Target;
import io.harness.cvdemo.behavior.MetricsGenerator;
import io.harness.cvdemo.metrics.CVDemoMetricsRegistry;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.SecureRandom;
import java.util.Collections;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static io.harness.cvdemo.metrics.Constants.LIST_RT;
import static io.harness.cvdemo.metrics.Constants.STATUS_RT;
import static io.harness.cvdemo.metrics.Constants.PROCESS_RT;
import static io.harness.cvdemo.payments.Constants.LIST;
import static io.harness.cvdemo.payments.Constants.STATUS;
import static io.harness.cvdemo.payments.Constants.PROCESS;
import static io.harness.cvdemo.metrics.Constants.LIST_ERRORS;
import static io.harness.cvdemo.metrics.Constants.STATUS_ERRORS;
import static io.harness.cvdemo.metrics.Constants.PROCESS_ERRORS;


@Slf4j
@Path("v1/payments")
@Produces(MediaType.APPLICATION_JSON)
public class PaymentsResource {

    private SecureRandom r = new SecureRandom();

    @Inject
    private CVDemoMetricsRegistry metricRegistry;
    protected static Client client;

    @GET
    @Path("list")
    public Response executeNormalCall(@QueryParam("bug") Boolean bug) {
        int max = 400, min = 200;

        try {
            if(bug == null ) {
                bug = false;
            }

            if (bug) {
                log.info("FF - list bug is enabled");
                max = 5300;
                min = 3550;
            }
        }catch(Exception e){
            log.error("FF bug list failed");
            log.error(e.toString());
            log.error(e.getMessage());
        }

        int msDelay = r.nextInt((max - min) + 1) + min;
        try {
            Thread.sleep(msDelay);

            metricRegistry.recordCounterInc(LIST, null);
            //log.info( "DIEGO -- " + metricRegistry.getMetric(Collections.singleton(LIST)).toString() );

            if (r.nextInt((100 - 1) + 1) < 4) {
                metricRegistry.recordGaugeValue(LIST_RT, null, msDelay);
                metricRegistry.recordGaugeInc(LIST_ERRORS, null);
                return Response.serverError().build();
            }
            metricRegistry.recordGaugeValue(LIST_RT, null, msDelay);
            return Response.ok().build();
        } catch (InterruptedException ex) {
            metricRegistry.recordGaugeInc(LIST_ERRORS, null);
            metricRegistry.recordGaugeValue(LIST_RT, null, msDelay);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            metricRegistry.recordGaugeInc(LIST_ERRORS, null);
            metricRegistry.recordGaugeValue(LIST_RT, null, msDelay);
            return Response.serverError().build();
        }
        metricRegistry.recordGaugeInc(LIST_ERRORS, null);
        metricRegistry.recordGaugeValue(LIST_RT, null, msDelay);
        return Response.serverError().build();
    }

    @GET
    @Path("status")
    public Response executeErrorCall(@QueryParam("value") double value,@QueryParam("bug") Boolean bug) {


        int max = 200, min = 40;

        try {
            if(bug == null ) {
                bug = false;
            }

            if (bug) {
                log.info("FF - status bug is enabled");
                max = 3300;
                min = 2550;
            }
        }catch(Exception e){
            log.error("FF bug status failed");
            log.error(e.toString());
            log.error(e.getMessage());

        }


        int msDelay = r.nextInt((max - min) + 1) + min;
        try {
            Thread.sleep(msDelay);
            metricRegistry.recordGaugeInc(STATUS, null);

            if (r.nextInt((100 - 1) + 1) < 20) {
                metricRegistry.recordGaugeValue(STATUS_RT, null, msDelay);
                metricRegistry.recordGaugeInc(STATUS_ERRORS, null);
                return Response.serverError().build();
            }
            metricRegistry.recordGaugeValue(STATUS_RT, null, msDelay);
            return Response.ok().build();
        } catch (InterruptedException ex) {
            metricRegistry.recordGaugeValue(STATUS_RT, null, msDelay);
            metricRegistry.recordGaugeInc(STATUS_ERRORS, null);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            metricRegistry.recordGaugeInc(STATUS_ERRORS, null);
            metricRegistry.recordGaugeValue(STATUS_RT, null, msDelay);
            return Response.serverError().build();
        }
        metricRegistry.recordGaugeInc(STATUS_ERRORS, null);
        metricRegistry.recordGaugeValue(STATUS_RT, null, msDelay);
        return Response.serverError().build();
    }


    @GET
    @Path("process")
    public Response executeDelayedCall(@QueryParam("value") double value,@QueryParam("bug") Boolean bug) {
        int max = 900, min = 300;

        try {
            if(bug == null ) {
                bug = false;
            }


            if (bug) {
                log.info("FF - process bug is enabled");
                max = 6300;
                min = 4550;
            }
        }catch(Exception e){
            log.error("FF bug process failed");
            log.error(e.toString());
            log.error(e.getMessage());

        }


        int msDelay = r.nextInt((max - min) + 1) + min;


        try {
            Thread.sleep(msDelay);
            metricRegistry.recordGaugeInc(PROCESS, null);
            if (client == null) {
                client =
                        ClientBuilder.newBuilder().hostnameVerifier((s1, s2) -> true).build();
            }

            WebTarget getPaymentTarget = client.target("https://olinda.bcb.gov.br/olinda/servico/PTAX/versao/v1/odata/CotacaoDolarDia(dataCotacao=@dataCotacao)?@dataCotacao=%2711-09-2022%27&$top=101&$format=json&$select=cotacaoVenda");
            Invocation.Builder invocationBuilder = getPaymentTarget.request();
            invocationBuilder.header("Accept", "application/json, text/plain, */*");
            invocationBuilder.get();

            if (r.nextInt((100 - 1) + 1) < 2) {
                metricRegistry.recordGaugeValue(PROCESS_RT, null, msDelay);
                metricRegistry.recordGaugeInc(PROCESS_ERRORS, null);
                return Response.serverError().build();
            }
            metricRegistry.recordGaugeValue(PROCESS_RT, null, msDelay);
            return Response.ok().build();
        } catch (InterruptedException ex) {
            metricRegistry.recordGaugeInc(PROCESS_ERRORS, null);
            metricRegistry.recordGaugeValue(PROCESS_RT, null, msDelay);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            metricRegistry.recordGaugeInc(PROCESS_ERRORS, null);
            metricRegistry.recordGaugeValue(PROCESS_RT, null, msDelay);
            return Response.serverError().build();
        }
        metricRegistry.recordGaugeInc(PROCESS_ERRORS, null);
        metricRegistry.recordGaugeValue(PROCESS_RT, null, msDelay);
        return Response.serverError().build();
    }
}
