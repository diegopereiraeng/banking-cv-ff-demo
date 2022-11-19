package io.harness.cvdemo.config.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ElkLogPublishConfig {
    private String elkUrl;
    private String elkPass;
    private String elkIndex;
    private String ffApiKey;
    private String ffMetricKey;
    private String ffLogKey;
    private String target;
}
