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


@Slf4j
public class MetricsGenerator implements Runnable {
  private final MetricConfig metricConfig;
  protected Client client;
  private final ElkLogPublishConfig elkLogPublishConfig;
  private SecureRandom r = new SecureRandom();

  private CfClient cfClient;

  public MetricsGenerator(MetricConfig metricConfig, ElkLogPublishConfig elkLogPublishConfig) {
    this.metricConfig = metricConfig;
    this.elkLogPublishConfig = elkLogPublishConfig;
    if (client == null) {
      client =
          ClientBuilder.newBuilder().hostnameVerifier((s1, s2) -> true).build();
    }
  }

  public void paymentGenerator(){
    // List Payments
    WebTarget getPaymentTarget = client.target("http://localhost:8080"
            + "/v1/payments/list");
    getPaymentTarget.request().get();

    // Maybe get payment status
    if (r.nextInt(2) <= 0){
      getPaymentTarget = client.target("http://localhost:8080"
              + "/v1/payments/status");
      getPaymentTarget.request().get();
    }
    // Maybe payment process
    if (r.nextInt(100) <= 40) {
      getPaymentTarget = client.target("http://localhost:8080"
              + "/v1/payments/process");
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


      Target target = Target.builder().name("MetricsGenerator").identifier("diego.pereira@harness.io").build();
      /**
       * Define you target on which you would like to evaluate the featureFlag
       */


      for (int i = 0; i < metricConfig.getCallsPerMinute(); i++) {

        boolean result;
        try {
          paymentGenerator();
        }catch (Exception e){
          log.error("Payments Generator Error");
        }
        result = App.behaviorGenerator.checkFlag(elkLogPublishConfig.getFfMetricKey());
        log.info("FF Metric Boolean variation for target" + elkLogPublishConfig.getTarget()+ " is " + result );

        WebTarget getTarget = client.target("http://localhost:8080"
                + "/metric/normal-call");
        if (result && r.nextInt((100 - 1) + 1) < 50) {
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


        // Banking Calls
        log.info("Banking Calls");
        getTarget = client.target("http://localhost:8080"+"/v1/payments/list");
        getTarget.request().get();
        getTarget = client.target("http://localhost:8080"+"/v1/payments/status");
        getTarget.request().get();
        getTarget = client.target("http://localhost:8080"+"/v1/payments/process");
        getTarget.request().get();


        Boolean externalTransaction = cfClient.boolVariation("external_transaction",target,false);

        if (externalTransaction){
          log.info("External Transaction Enabled");
          WebTarget getTransactions = client.target(cfClient.stringVariation("transaction_url",target,"http://localhost:8080/metric/normal-call"));

          log.info("External Transaction Status: "+getTransactions.request().get().getStatus());
        }
        else {
          log.warn("External Transaction Disabled");
        }

        Thread.sleep(60000 / metricConfig.getCallsPerMinute());
      }
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      log.error(e.getMessage());
      throw e;
    }

  }
}
