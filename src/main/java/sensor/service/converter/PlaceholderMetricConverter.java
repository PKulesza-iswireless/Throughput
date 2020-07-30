package sensor.service.converter;

import lombok.AllArgsConstructor;
import org.apache.commons.text.StrSubstitutor;
import sensor.common.pojo.MetricPOJO;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class PlaceholderMetricConverter implements MetricConverter {

    private String pattern;

    @Override
    public String convert(MetricPOJO metricPOJO) {
        return StrSubstitutor.replace(pattern, getAsMap(metricPOJO));
    }

    private Map<String, String> getAsMap(MetricPOJO metricPOJO) {
        Map<String, String> result = new HashMap<>();
        result.put("metric.name", metricPOJO.getMetricName());
        result.put("metric.value", Double.toString(metricPOJO.getMetricValue()));
        result.put("metric.timestamp", Long.toString(metricPOJO.getTimestamp()));
        return result;
    }
}

