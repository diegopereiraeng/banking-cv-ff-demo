package io.harness.cvdemo.behavior;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.harness.cvdemo.config.beans.ElkLogPublishConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class LogPublisher {
    private ElkLogPublishConfig elkLogPublishConfig;
    static CloseableHttpClient httpclient = HttpClients.createDefault();

    public LogPublisher(ElkLogPublishConfig publishConfig) {
        this.elkLogPublishConfig = publishConfig;
    }

    static class    LogData {
        String hostname;
        String level;
        String message;
    }

    public void publishLogs(String level, String logMsg) throws IOException {
        log.info("Log Publisher -  elkurl: "+elkLogPublishConfig.getElkUrl());
        log.info("Log Publisher -  elkIndex: "+elkLogPublishConfig.getElkIndex());
        if (StringUtils.isNotEmpty(elkLogPublishConfig.getElkUrl())
                && StringUtils.isNotEmpty(elkLogPublishConfig.getElkIndex())) {

            log.info("Log publisher - started");

            String elkUrlToPost = elkLogPublishConfig.getElkUrl() + elkLogPublishConfig.getElkIndex() + "/_doc";
            LogData logData = new LogData();
            logData.hostname = InetAddress.getLocalHost().getHostName();
            logData.level = level;
            logData.message = logMsg;

            Gson gson = new Gson();
            JsonElement jsonElement = gson.toJsonTree(logData);
            JsonObject jsonObject = (JsonObject) jsonElement;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
            Date now = new Date();
            jsonObject.addProperty("@timestamp", "" + sdf.format(now));

            String outMsg = jsonObject.toString();
            outMsg = outMsg.replaceAll("\n", " ").replaceAll("\t", " ");
            HttpPost httpPost = new HttpPost(elkUrlToPost);
            httpPost.setEntity(new StringEntity(outMsg, ContentType.APPLICATION_JSON));
            httpPost.setHeader("hostname", InetAddress.getLocalHost().getHostName());
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Authorization",elkLogPublishConfig.getElkPass());

            log.info("Log Publisher -  url call: "+elkUrlToPost);
            log.info("Log Publisher -  json: "+outMsg);

            //CloseableHttpResponse response = httpclient.execute(httpPost);


            CloseableHttpResponse response;

            // metodo 1
            SSLContextBuilder builder = new SSLContextBuilder();
            try {
                builder.loadTrustMaterial(null, new TrustAllStrategy());
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                        builder.build(), NoopHostnameVerifier.INSTANCE);
                CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                        sslsf).build();
                response = httpclient.execute(httpPost);
                System.out.println("Log Publisher -  "+response.getStatusLine());
                HttpEntity entity = response.getEntity();
                EntityUtils.consume(entity);
                log.info("Log Publisher -  "+response.getEntity().getContent().toString());
                response.close();

            } catch (NoSuchAlgorithmException e) {
                log.error("Log Publisher -  Error");
                throw new RuntimeException(e);
            } catch (KeyStoreException e) {
                log.error("Log Publisher -  Error");
                throw new RuntimeException(e);
            } catch (KeyManagementException e) {
                log.error("Log Publisher -  Error");
                throw new RuntimeException(e);
            }





            // metodo 2

            CloseableHttpClient httpClient2;
            try {
                httpClient2 = HttpClients.custom()
                        .setSSLSocketFactory(new SSLConnectionSocketFactory(SSLContexts.custom()
                                        .loadTrustMaterial(null, new TrustAllStrategy())
                                        .build()
                                )
                        ).build();
            } catch (NoSuchAlgorithmException e) {
                log.error("Log Publisher 2 -  Error");
                throw new RuntimeException(e);
            } catch (KeyManagementException e) {
                log.error("Log Publisher 2 -  Error");
                throw new RuntimeException(e);
            } catch (KeyStoreException e) {
                log.error("Log Publisher 2 -  Error");
                throw new RuntimeException(e);
            }

            response = httpClient2.execute(httpPost);

            try {
                System.out.println("Log Publisher 2 -  "+response.getStatusLine());
                HttpEntity entity = response.getEntity();
                EntityUtils.consume(entity);


            } finally {
                log.info("Log Publisher 2 -  "+response.getEntity().getContent().toString());
                response.close();
            }

            // metodo 3

            CloseableHttpClient httpClient3;

            try {
                httpClient3 = HttpClients
                        .custom()
                        .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                        .build();
            } catch (NoSuchAlgorithmException e) {
                log.error("Log Publisher 3 -  Error");
                throw new RuntimeException(e);
            } catch (KeyManagementException e) {
                log.error("Log Publisher 3 -  Error");
                throw new RuntimeException(e);
            } catch (KeyStoreException e) {
                log.error("Log Publisher 3 -  Error");
                throw new RuntimeException(e);
            }


            response = httpClient3.execute(httpPost);

            try {
                System.out.println("Log Publisher 3 -  "+response.getStatusLine());
                HttpEntity entity = response.getEntity();
                EntityUtils.consume(entity);


            } finally {
                log.info("Log Publisher 3 -  "+response.getEntity().getContent().toString());
                response.close();
            }




            //response.close();
        }
        else{
            log.warn("Log Publisher - Disabled");
        }
    }
}
