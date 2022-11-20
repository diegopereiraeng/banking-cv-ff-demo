package io.harness.cvdemo.behavior;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.harness.cvdemo.config.beans.ElkLogPublishConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.InetAddress;
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
        log.info("Log configuration: elkurl: "+elkLogPublishConfig.getElkUrl());
        log.info("Log configuration: elkIndex: "+elkLogPublishConfig.getElkIndex());
        if (StringUtils.isNotEmpty(elkLogPublishConfig.getElkUrl())
                && StringUtils.isNotEmpty(elkLogPublishConfig.getElkIndex())) {

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
            CloseableHttpResponse response = httpclient.execute(httpPost);

            log.info("Log configuration: url call: "+elkUrlToPost);
            log.info("Log configuration: json: "+outMsg);


            response.close();
        }
        else{
            log.warn("Log warning - Log Disabled");
        }
    }
}
