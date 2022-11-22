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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.Arrays;
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
    public Response paymentList(@QueryParam("bug") Boolean bug) {
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

            metricRegistry.recordGaugeInc(LIST, null);
            //log.info( "DIEGO -- " + metricRegistry.getMetric(Collections.singleton(LIST)).toString() );

            if (r.nextInt((100 - 1) + 1) < 5) {
                metricRegistry.recordGaugeValue(LIST_RT, null, msDelay);
                metricRegistry.recordGaugeInc(LIST_ERRORS, null);
                log.error("ERROR [Payment List] - Bug Demo");
                return Response.serverError().build();
            }
            metricRegistry.recordGaugeValue(LIST_RT, null, msDelay);
            return Response.ok().build();
        } catch (InterruptedException ex) {
            metricRegistry.recordGaugeInc(LIST_ERRORS, null);
            metricRegistry.recordGaugeValue(LIST_RT, null, msDelay);
            log.error("ERROR [Payment List] - Bug Demo");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            metricRegistry.recordGaugeInc(LIST_ERRORS, null);
            metricRegistry.recordGaugeValue(LIST_RT, null, msDelay);
            log.error("ERROR [Payment List] - Bug Demo");
            return Response.serverError().build();
        }
        metricRegistry.recordGaugeInc(LIST_ERRORS, null);
        metricRegistry.recordGaugeValue(LIST_RT, null, msDelay);
        log.error("ERROR [Payment List] - Bug Demo");
        return Response.serverError().build();
    }

    @GET
    @Path("status")
    public Response paymentStatus(@QueryParam("value") double value,@QueryParam("bug") Boolean bug) {


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

            if (r.nextInt((100 - 1) + 1) < 5) {
                metricRegistry.recordGaugeValue(STATUS_RT, null, msDelay);
                metricRegistry.recordGaugeInc(STATUS_ERRORS, null);
                log.error("ERROR [Payment Status] - Bug Demo");
                return Response.serverError().build();
            }
            metricRegistry.recordGaugeValue(STATUS_RT, null, msDelay);
            return Response.ok().build();
        } catch (InterruptedException ex) {
            metricRegistry.recordGaugeValue(STATUS_RT, null, msDelay);
            metricRegistry.recordGaugeInc(STATUS_ERRORS, null);
            log.error("ERROR [Payment Status] - Bug Demo");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            metricRegistry.recordGaugeInc(STATUS_ERRORS, null);
            metricRegistry.recordGaugeValue(STATUS_RT, null, msDelay);
            log.error("ERROR [Payment Status] - Bug Demo");
            return Response.serverError().build();
        }
        metricRegistry.recordGaugeInc(STATUS_ERRORS, null);
        metricRegistry.recordGaugeValue(STATUS_RT, null, msDelay);
        log.error("ERROR [Payment Status] - Bug Demo");
        return Response.serverError().build();
    }

    private String getVersion(){
        String version = "stable";
        try {
            InetAddress inetAdd = InetAddress.getLocalHost();

            String name = inetAdd.getHostName();

            log.info("Diego - hostname: "+name);

            if(name.contains("canary")){
                log.info("Diego - Set as Canary");
                version = "canary";
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        return version;
    }

    @GET
    @Path("process")
    public Response paymentProcess(@QueryParam("value") double value,@QueryParam("bug") Boolean bug) {
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

            // Generate bugs in randon mode 75%<
            if (r.nextInt((100 - 1) + 1) < 75) {
                metricRegistry.recordGaugeValue(PROCESS_RT, null, msDelay);
                metricRegistry.recordGaugeInc(PROCESS_ERRORS, null);
                log.error("ERROR [Payment Process] - Bug Alex");
                return Response.serverError()
                        .status(Response.Status.UNAUTHORIZED)
                        .entity("Bug Alex - "+this.getVersion())
                        .build();
            }

            metricRegistry.recordGaugeValue(PROCESS_RT, null, msDelay);
            return Response.ok().entity("Payment Accepted - version: "+this.getVersion()).build();
        } catch (InterruptedException ex) {
            metricRegistry.recordGaugeInc(PROCESS_ERRORS, null);
            metricRegistry.recordGaugeValue(PROCESS_RT, null, msDelay);
            log.error("ERROR [Payment Process] - Interrupted");
            return Response.serverError()
                    .status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("Transaction Interrupted, Try Again!")
                    .build();
        } catch (Exception e) {
            metricRegistry.recordGaugeInc(PROCESS_ERRORS, null);
            metricRegistry.recordGaugeValue(PROCESS_RT, null, msDelay);
            log.error("ERROR [Payment Process] - Conflict");
            return Response.serverError()
                    .status(Response.Status.CONFLICT)
                    .entity("Please talk to your bank manager.")
                    .build();
        }

    }
}

