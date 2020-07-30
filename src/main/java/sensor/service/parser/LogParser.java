package sensor.service.parser;

import sensor.common.pojo.MetricPOJO;

public interface LogParser {

    MetricPOJO parseLineToPOJO(String line);
}

