package io.harness.cvdemo.payments;

import com.google.inject.Inject;
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



import static io.harness.cvdemo.payments.Constants.LIST;
import static io.harness.cvdemo.payments.Constants.STATUS;
import static io.harness.cvdemo.payments.Constants.PROCESS;




@Path("v1/payments")
@Produces(MediaType.APPLICATION_JSON)
public class PaymentsResource {

    private SecureRandom r = new SecureRandom();

    @Inject
    private CVDemoMetricsRegistry metricRegistry;
    protected static Client client;

    @GET
    @Path("list")
    public Response executeNormalCall() {
        int max = 2500, min = 1200;
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
    public Response executeErrorCall(@QueryParam("value") double value) {


        int max = 300, min = 40;
        int msDelay = r.nextInt((max - min) + 1) + min;
        try {
            Thread.sleep(msDelay);
            metricRegistry.recordGaugeValue(STATUS, null, value);

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

    @GET
    @Path("process")
    public Response executeDelayedCall(@QueryParam("value") double value) {
        int max = 1900, min = 400;
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
