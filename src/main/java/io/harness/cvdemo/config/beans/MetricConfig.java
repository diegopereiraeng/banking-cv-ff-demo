package io.harness.cvdemo.config.beans;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(NON_NULL)
public class MetricConfig {
  private int callsPerMinute;
  private double errorRate;
  private double minErrorValue;
  private double maxErrorValue;
  private boolean shouldDoDelayedCalls;
  private String name;
}
