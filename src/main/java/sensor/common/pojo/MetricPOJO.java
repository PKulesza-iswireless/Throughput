package sensor.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MetricPOJO {
    private String metricName;
    private double metricValue;
    private long timestamp;

}
