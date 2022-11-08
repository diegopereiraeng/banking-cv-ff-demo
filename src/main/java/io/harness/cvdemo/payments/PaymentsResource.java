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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


import static io.harness.cvdemo.payments.Constants.LIST;
import static io.harness.cvdemo.payments.Constants.STATUS;
import static io.harness.cvdemo.payments.Constants.PROCESS;




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
            metricRegistry.recordGaugeValue(LIST, null, 1);

            return Response.ok().build();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            return Response.serverError().build();
        }
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
            metricRegistry.recordGaugeValue(STATUS, null, value);

            if (r.nextInt((100 - 1) + 1) < 20) {
                return Response.serverError().build();
            }

            return Response.ok().build();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            return Response.serverError().build();
        }
        return Response.serverError().build();
    }

    @GET
    @Path("process")
    public Response executeDelayedCall(@QueryParam("value") double value,@QueryParam("bug") Boolean bug) {
        int max = 1000, min = 300;



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
            metricRegistry.recordGaugeValue(PROCESS, null, 1);
            if (client == null) {
                client =
                        ClientBuilder.newBuilder().hostnameVerifier((s1, s2) -> true).build();
            }

            WebTarget getPaymentTarget = client.target("https://olinda.bcb.gov.br/olinda/servico/PTAX/versao/v1/odata/CotacaoDolarDia(dataCotacao=@dataCotacao)?@dataCotacao='09-10-2021'&$top=100&$skip=0&$format=json&$select=cotacaoCompra");
            Invocation.Builder invocationBuilder = getPaymentTarget.request();
            invocationBuilder.header("x-rapidapi-host", "apidojo-yahoo-finance-v1.p.rapidapi.com");
            invocationBuilder.header("x-rapidapi-key", "b21b7e3fc6msh1d56993ae6e9e37p11a84bjsn059026f4667e");
            invocationBuilder.get();

            if (r.nextInt((100 - 1) + 1) < 50) {
                return Response.serverError().build();
            }

            return Response.ok().build();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            return Response.serverError().build();
        }
        return Response.serverError().build();
    }
}
