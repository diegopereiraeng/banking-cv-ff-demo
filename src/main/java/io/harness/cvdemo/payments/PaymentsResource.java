package io.harness.cvdemo.payments;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.harness.cvdemo.metrics.CVDemoMetricsRegistry;
import io.harness.cvdemo.models.Payment;
import io.harness.cvdemo.models.Representation;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.io.ssl.ALPNProcessor;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;

import static io.harness.cvdemo.metrics.Constants.*;
import static io.harness.cvdemo.payments.Constants.LIST;
import static io.harness.cvdemo.payments.Constants.PROCESS;
import static io.harness.cvdemo.payments.Constants.STATUS;


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

        String status = "200";
        String exception = "unknown";

        int msDelay = r.nextInt((max - min) + 1) + min;
        try {
            Thread.sleep(msDelay);

            //log.info( "DIEGO -- " + metricRegistry.getMetric(Collections.singleton(LIST)).toString() );

            if (r.nextInt((100 - 1) + 1) < 5) {
                status = "500";
            }

        } catch (InterruptedException ex) {
            status = "500";
            exception = "Interruption";

        } catch (Exception e) {
            status = "500";
        }finally {
            if (status != "200"){
                log.error("ERROR [Payment List] - List Exception");
                metricRegistry.recordGaugeValue(LIST_RT, null, msDelay);
                metricRegistry.recordGaugeInc(LIST_ERRORS, null);
                if (exception == "Interruption"){
                    Thread.currentThread().interrupt();
                }
                return Response.serverError().entity("ERROR [Payment List] - List Exception: "+exception).build();
            }else{


                metricRegistry.recordGaugeValue(LIST_RT, null, msDelay);
                metricRegistry.recordGaugeInc(LIST, null);
                return Response.ok().entity("Payment List - version: "+this.getVersion()).build();
            }

        }

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

        String status = "200";
        String exception = "unknown";

        int msDelay = r.nextInt((max - min) + 1) + min;
        try {
            Thread.sleep(msDelay);
            metricRegistry.recordGaugeInc(STATUS, null);

            if (r.nextInt((100 - 1) + 1) < 5) {
                status = "500";
            }

        } catch (InterruptedException ex) {
            status = "500";
            exception = "Interruption";

        } catch (Exception e) {
            status = "500";
        }
        finally {
            if (status != "200"){
                log.error("ERROR [Payment Status] - Status Error");
                metricRegistry.recordGaugeValue(STATUS_RT, null, msDelay);
                metricRegistry.recordGaugeInc(STATUS_ERRORS, null);
                if (exception == "Interruption"){
                    Thread.currentThread().interrupt();
                }
                return Response.serverError().entity("ERROR [Payment Status] - Status Error: "+exception).build();
            }else{


                metricRegistry.recordGaugeValue(LIST_RT, null, msDelay);
                metricRegistry.recordGaugeInc(STATUS, null);
                return Response.ok().entity("ERROR [Payment Status] - Status Error: "+this.getVersion()).build();
            }

        }

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
    public Response paymentProcess(@QueryParam("value") double value, @QueryParam("bug") Boolean bug, @QueryParam("validationPath") String validationPath, @QueryParam("validationID") String validationID,@QueryParam("invoiceID") int invoiceID) {
        int max = 200, min = 100;
        boolean validated = false;

        try {
            if(bug == null ) {
                bug = false;
            }

            if(validationID == null ) {
                validationID = "";
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

        if(invoiceID + 0 == 0 ) {
            invoiceID = msDelay;
        }

        try {
            Thread.sleep(msDelay);
            metricRegistry.recordGaugeInc(PROCESS, null);
            if (client == null) {
                client =
                        ClientBuilder.newBuilder().hostnameVerifier((s1, s2) -> true).build();
            }

            if(validationPath == null ) {
                validationPath = "demo-se";
            }


//            // Generate bugs in randon mode 75%<
//            if (r.nextInt((100 - 1) + 1) < 5) {
//                metricRegistry.recordGaugeValue(PROCESS_RT, null, msDelay);
//                metricRegistry.recordGaugeInc(PROCESS_ERRORS, null);
//                log.error("ERROR [Payment Process] - Bug Demo");
//                return Response.serverError()
//                        .status(Response.Status.INTERNAL_SERVER_ERROR)
//                        .entity("[Payment Process] - Bug Demo - "+this.getVersion())
//                        .build();
//            }

            WebTarget ValidationAPI = client.target("http://payments-validation.harness-demo.site/"+validationPath+"/auth/validation");
            Invocation.Builder invocationBuilder = ValidationAPI.request();
            invocationBuilder.header("Accept", "application/json, text/plain, */*");
            Response listValidations =  invocationBuilder.get();
            log.info("[Validation Journey] - Status: "+listValidations.getStatus()+" from Validation HealthCheck on: "+validationPath );
            log.info("[Validation Journey] - ValidationID "+validationID);

            if(listValidations.getStatus() == 200){
                ValidationAPI = client.target("http://payments-validation.harness-demo.site/"+validationPath+"/auth/validation");
                invocationBuilder = ValidationAPI.request();
                String jsonString = new JSONObject()
                        .put("id", invoiceID)
                        .put("status", "not verified")
                        .put("validationID", validationID)
                        //.put("JSON3", new JSONObject().put("key1", "value1"))
                        .toString();
                invocationBuilder.header("Accept", "application/json, text/plain, */*");
                Response validation =  invocationBuilder.post(Entity.json(jsonString));
                String validationBody = validation.readEntity(String.class);
                log.debug("[Validation Journey] - Response: "+validationBody);
                // Map String to Representation
                ObjectMapper mapper = new ObjectMapper();
                //Representation responseRepPay = mapper.readValue(validationBody, Representation.class);
                //mapper.enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES.mappedFeature());
                JsonNode validationNode = mapper.readTree(validationBody);
                Representation<Payment> responseRepPay = mapper.treeToValue(validationNode,Representation.class);
                log.debug("[Validation Journey] - Response Parsed Rep: "+responseRepPay.getData());
                Payment responsePay = mapper.treeToValue(validationNode.get("data"),Payment.class);
                log.debug("[Validation Journey] - Response Parsed: "+responsePay.getVersion());

                if(validation.getStatus() == 200){
                    validated = true;
                    log.debug("[Validation Journey] - Invoice "+invoiceID+" Validated");
                    WebTarget getPaymentTarget = client.target("https://olinda.bcb.gov.br/olinda/servico/PTAX/versao/v1/odata/CotacaoDolarDia(dataCotacao=@dataCotacao)?@dataCotacao=%2711-09-2022%27&$top=101&$format=json&$select=cotacaoVenda");
                    invocationBuilder = getPaymentTarget.request();
                    invocationBuilder.header("Accept", "application/json, text/plain, */*");
                    Response response = invocationBuilder.get();

                    //log.info("[Validation Journey] - "+response.getEntity().toString());

                    if(validation.getStatus() == 200){
                        metricRegistry.recordGaugeValue(PROCESS_RT, null, msDelay);

                        //return Response.ok().entity("Payment Accepted - version: "+this.getVersion()).build();
                        return Response.ok().entity("Payment Accepted - version: "+this.getVersion()+" - auth version: "+responsePay.getVersion()).build();
                    }


                    log.error("ERROR [Payment Validation] - Invoice Denied - Try again later");
                    return Response.serverError()
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("ERROR [Payment Validation] - Invoice Denied - Try again later - "+this.getVersion()+" - auth version: "+responsePay.getVersion())
                            .build();
//                    return Response.serverError()
//                            .status(Response.Status.INTERNAL_SERVER_ERROR)
//                            .entity("ERROR [Payment Validation] - Invoice Denied - Try again later - "+this.getVersion())
//                            .build();


                }
                else {
                    metricRegistry.recordGaugeValue(PROCESS_RT, null, msDelay);
                    metricRegistry.recordGaugeInc(PROCESS_ERRORS, null);
                    log.error("ERROR [Payment Validation] - Invoice Denied - Try again later");
                    return Response.serverError()
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("ERROR [Payment Validation] - Invoice Denied - Try again later - "+this.getVersion())
                            .build();
                }
            }else{
                metricRegistry.recordGaugeValue(PROCESS_RT, null, msDelay);
                metricRegistry.recordGaugeInc(PROCESS_ERRORS, null);
                log.error("ERROR [Payment Validation] - Validation App not available");
                return Response.serverError()
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("ERROR [Payment Validation] - Validation App not available - "+this.getVersion())
                        .build();
            }


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
            log.error("ERROR [Payment Process] - Conflict Error");
            e.printStackTrace();
            return Response.serverError()
                    .status(Response.Status.CONFLICT)
                    .entity("Conflict Error - Please talk to your bank manager.")
                    .build();
        }

    }
}

