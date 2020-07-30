package sensor.service.parser;

import lombok.AllArgsConstructor;
import sensor.common.pojo.MetricPOJO;

@AllArgsConstructor
public class GenomLogParser implements LogParser {

    private String metricName;

    @Override
    public MetricPOJO parseLineToPOJO(String line) {
        return null;
    }

}
