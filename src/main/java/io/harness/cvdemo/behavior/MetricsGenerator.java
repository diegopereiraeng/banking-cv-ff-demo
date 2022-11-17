package io.harness.cvdemo.behavior;

import io.harness.cf.client.api.CfClient;
import io.harness.cf.client.dto.Target;
import io.harness.cvdemo.App;
import io.harness.cvdemo.config.beans.ElkLogPublishConfig;
import io.harness.cvdemo.config.beans.MetricConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;


import java.security.SecureRandom;

//get hostname
import java.net.InetAddress;


@Slf4j
public class MetricsGenerator implements Runnable {
  private final MetricConfig metricConfig;
  protected Client client;
  private final ElkLogPublishConfig elkLogPublishConfig;
  private SecureRandom r = new SecureRandom();

  private CfClient cfClient ;

  public MetricsGenerator(MetricConfig metricConfig, ElkLogPublishConfig elkLogPublishConfig) {
    this.metricConfig = metricConfig;
    this.elkLogPublishConfig = elkLogPublishConfig;
    if (client == null) {
      client =
          ClientBuilder.newBuilder().hostnameVerifier((s1, s2) -> true).build();
    }
  }

  public void paymentGenerator(Boolean bug_list, Boolean bug_status, Boolean bug_process){
    // List Payments
    WebTarget getPaymentTarget = client.target("http://localhost:8080"
            + "/v1/payments/list?bug=false");
    if (bug_list){
      getPaymentTarget = client.target("http://localhost:8080"
              + "/v1/payments/list?bug=true");
    }
    getPaymentTarget.request().get();

    // Maybe get payment status
    if (r.nextInt(2) <= 0){

      if (bug_status){
        getPaymentTarget = client.target("http://localhost:8080"
                + "/v1/payments/status?bug=true&value="+r.nextInt(50));
      }
      else {
        getPaymentTarget = client.target("http://localhost:8080"
                + "/v1/payments/status?bug=false&value="+r.nextInt(50));
      }
      getPaymentTarget.request().get();
    }

    // Maybe payment process
    if (r.nextInt(100) <= 60) {

      if (bug_process){
        getPaymentTarget = client.target("http://localhost:8080"
                + "/v1/payments/process?bug=true&value="+r.nextInt(100));
      }else{
        getPaymentTarget = client.target("http://localhost:8080"
                + "/v1/payments/process?bug=false&value="+r.nextInt(100));
      }


      getPaymentTarget.request().get();
    }
  }

  @SneakyThrows
  @Override
  public void run() {

    try{

      /** FEATURE FLAGS **
       * Put the API Key here from your environment
       */
      //String apiKey = "4491708f-83b2-4695-8b7e-311f254f12b1";
      String apiKey = elkLogPublishConfig.getFfApiKey();

      log.info("FF ENV - check for ff key in env");
      String ffKey = System.getenv("FF_KEY");
      log.info("FF ENV - checked value: "+ffKey);
      String version = System.getenv("DD_VERSION");

      if ( version == null || version == ""){
        version = "MetricsGenerator";
      }

      cfClient = new CfClient(ffKey, io.harness.cf.client.api.Config.builder().build());



      // get podname
      InetAddress inetadd = InetAddress.getLocalHost();

      String name = inetadd.getHostName();

      String targetDeploy = "stable";
      log.info("Diego - hostname: "+name);

      if(name.contains("canary")){
        log.info("Diego - Set as Canary");
        targetDeploy = "canary";
        version = targetDeploy;
      }

      Target target = Target.builder().name(version).identifier(version).build();

      // get pod name end

      /**
       * Define you target on which you would like to evaluate the featureFlag
       */

      for (int i = 0; i < metricConfig.getCallsPerMinute(); i++) {

        boolean result;
        Boolean bug_process = false;
        Boolean bug_status = false;
        Boolean bug_list = false;

        try {
          if ( version == "canary"){


            log.info("FF - check if bug process is enabled");
            bug_process = cfClient.boolVariation("bug_process_response", target, false);
            log.info("FF - bug process is "+bug_process);

            log.info("FF - check if bug status is enabled");
            bug_status = cfClient.boolVariation("bug_status_response", target, false);
            log.info("FF - bug status is "+bug_status);

            log.info("FF - check if bug list is enabled");
            bug_list = cfClient.boolVariation("bug_list_response", target, false);
            log.info("FF - bug list is "+bug_list);

            log.info("FF - check if External Transaction Enabled");
            Boolean externalTransaction = cfClient.boolVariation("external_transaction",target,false);
            log.info("FF - checked if External Transaction Enabled");
            if (externalTransaction){
              log.info("External Transaction Enabled");
              WebTarget getTransactions = client.target(cfClient.stringVariation("transaction_url",target,"http://localhost:8080/metric/normal-call"));

              log.info("External Transaction Status: "+getTransactions.request().get().getStatus());
            }
            else {
              log.warn("External Transaction Disabled");
            }
          }
        }catch (Exception e){
          log.error("Metrics Generator Feature Flags Error -> External Calls Control and Bug Control");
        }

        try {

          paymentGenerator(bug_list,bug_status,bug_process);

        }catch (Exception e){
          log.error("Payments Generator Error");
        }
        try {
          result = App.behaviorGenerator.checkFlag(elkLogPublishConfig.getFfMetricKey());
          log.info("FF Metric Boolean variation for target" + elkLogPublishConfig.getTarget()+ " is " + result );

          WebTarget getTarget = client.target("http://localhost:8080"
                  + "/metric/normal-call");
          //if (result && r.nextInt((100 - 1) + 1) < 50) {
          if (result) {
            boolean result2 = App.behaviorGenerator.checkFlag(elkLogPublishConfig.getFfLogKey());
            double value = r.nextInt((1000 - 100) + 100);
            if (result2){
              value = r.nextInt((10000 - 1000) + 1000);
            }


            log.info("FF "+elkLogPublishConfig.getFfMetricKey()+" activated");
            getTarget = client.target("http://localhost:8080"
                    + "/metric/error-call?value=" + value);
          }
          else if (r.nextInt((100 - 1) + 1) < metricConfig.getErrorRate()) {
            double range = metricConfig.getMaxErrorValue() - metricConfig.getMinErrorValue();
            double value = r.nextInt() * range + metricConfig.getMinErrorValue();
            getTarget = client.target("http://localhost:8080"
                    + "/metric/error-call?value=" + value);
            /*// Initializing String variable with null value
            String ptr = null;

            // Checking if ptr.equals null or works fine.
            try
            {
              // This line of code throws NullPointerException
              // because ptr is null
              if (ptr.equals("gfg"))
                System.out.print("Same");
              else
                System.out.print("Not Same");
            }
            catch(NullPointerException e)
            {

              log.error("NullPointerException Caught");
              throw e;
            }*/
          }

          getTarget.request().get();
        }catch (Exception e){
          log.error("Metrics Generator Error (Prometheus metrics /normal-calls etc)");
        }

        try{
          // Banking Calls
          log.info("Banking Calls");
          WebTarget getTarget = client.target("http://localhost:8080"+"/v1/payments/list?bug="+bug_list);
          getTarget.request().get();
          getTarget = client.target("http://localhost:8080"+"/v1/payments/status?bug="+bug_status+"&value="+r.nextInt(100));
          getTarget.request().get();
/*          getTarget = client.target("http://localhost:8080"+"/v1/payments/process?bug=true&value="+r.nextInt(100));
          getTarget.request().get();*/

        }catch (Exception e){
          log.error("Metrics Generator Error (Banking Calls 2)");
        }

        log.info("FF - check if FF is initialized");






        Thread.sleep(55000 / metricConfig.getCallsPerMinute());
      }
    } catch (InterruptedException ex) {
      log.error(ex.getMessage());
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      log.error("Fail in generating Metric Behavior");
      log.error(e.getMessage());
      log.error(e.getCause().getMessage());
      log.error(e.getLocalizedMessage());
      log.error(e.toString());
    }

  }
}
