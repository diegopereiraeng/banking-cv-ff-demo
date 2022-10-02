FROM openjdk:8

# install yq - a YAML query command line tool
RUN curl -Lso yq https://github.com/mikefarah/yq/releases/download/2.2.1/yq_linux_amd64 && \
    chmod +x yq && \
    mv yq /usr/local/bin
COPY config.yml /opt/cv-demo/
COPY target/*.jar /opt/cv-demo/app.jar
#COPY AppServerAgent-4.5.0.23604.tar.gz  /opt/cv-demo/AppServerAgent-4.5.0.23604.tar.gz
COPY harness-et-agent /opt/harness-et-agent
ENV JAVA_TOOL_OPTIONS="-agentpath:/opt/harness-et-agent/lib/libETAgent.so"
ENV ET_COLLECTOR_URL=https://app.harness.io/prod2/et-collector/
ENV ET_APPLICATION_NAME=FF_CV_DEMO
ENV ET_DEPLOYMENT_NAME=1
ENV ET_ENV_ID=gitflow
ENV ET_ACCOUNT_ID=Io9SR1H7TtGBq9LVyJVB2w
ENV ET_ORG_ID=default
ENV ET_PROJECT_ID=FF_GITFLOW_CV
#RUN wget -qO- https://get.et.harness.io/releases/latest/nix/harness-et-agent.tar.gz | tar -xz
#COPY newrelic-java-5.3.0.tar.gz /opt/cv-demo/

WORKDIR /opt/cv-demo

CMD bash -c ' \
    if [[ "$ENABLE_APPDYNAMICS" == "true" ]]; then \
      tar -xvzf AppServerAgent-4.5.0.23604.tar.gz; \
      node_name="-Dappdynamics.agent.nodeName=$(hostname)"; \
      JAVA_OPTS=$JAVA_OPTS" -javaagent:/opt/cv-demo/AppServerAgent-4.5.0.23604/javaagent.jar -Dappdynamics.jvm.shutdown.mark.node.as.historical=true"; \
      JAVA_OPTS="$JAVA_OPTS $node_name"; \
      echo "Using Appdynamics java agent"; \
    fi; \
    \
    if [[ "$ENABLE_NEWRELIC" == "true" ]]; then \
      tar -zxvf newrelic-java-5.3.0.tar.gz; \
      JAVA_OPTS=$JAVA_OPTS" -javaagent:/opt/cv-demo/newrelic/newrelic.jar"; \
    fi; \
    \
    CONFIG_FILE=/opt/cv-demo/config.yml; \
    if [[ "" != "$ALLOWED_ORIGINS" ]]; then yq write -i $CONFIG_FILE allowedOrigins "$ALLOWED_ORIGINS"; fi; \
    if [[ "" != "$ELK_URL" ]]; then yq write -i $CONFIG_FILE elkUrl "$ELK_URL"; fi; \
    if [[ "" != "$ELK_INDEX" ]]; then yq write -i $CONFIG_FILE elkIndex "$ELK_INDEX"; fi; \
    if [[ "" != "$FF_API_KEY" ]]; then yq write -i $CONFIG_FILE ffApiKey "$FF_API_KEY"; fi; \
    if [[ "" != "$FF_METRIC_KEY" ]]; then yq write -i $CONFIG_FILE ffMetricKey "$FF_METRIC_KEY"; fi; \
    if [[ "" != "$FF_LOG_KEY" ]]; then yq write -i $CONFIG_FILE ffLogKey "$FF_LOG_KEY"; fi; \
    if [[ "" != "$FF_TARGET" ]]; then yq write -i $CONFIG_FILE target "$FF_TARGET"; fi; \
    if [[ "" != "$LOG_MSG" ]]; then yq write -i $CONFIG_FILE defaultConfig.logConfig.errorMessage "LOG_MSG"; fi; \
    java $JAVA_OPTS -jar app.jar server config.yml'