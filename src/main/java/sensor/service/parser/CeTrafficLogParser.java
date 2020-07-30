package sensor.service.parser;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sensor.common.pojo.MetricPOJO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@AllArgsConstructor
public class CeTrafficLogParser implements LogParser {

    private final static String LOGS_LINE_REGEX =  "^((?<metricName>.+?) (?<value>.+?) (?<timestamp>\\d{10}))$";
    private final static String BLANK_LINE_REGEX = "^\\s*$";

    private final static Pattern LOGS_LINE_PATTERN = Pattern.compile(LOGS_LINE_REGEX);
    private final static Pattern BLANK_LINE_PATTERN = Pattern.compile(BLANK_LINE_REGEX);


    @Override
    public MetricPOJO parseLineToPOJO(String line) {
        log.debug("line: {}", line);

        if (line == null || BLANK_LINE_PATTERN.matcher(line).find()){
            log.info("line is blank");
            return null;
        }

        Matcher logsLineMatcher =  LOGS_LINE_PATTERN.matcher(line);
        if(logsLineMatcher.find()){
            String metricName = logsLineMatcher.group("metricName");
            String value = logsLineMatcher.group("value");
            String timestamp = logsLineMatcher.group("timestamp");

            log.debug("name: {}, value: {}, timestamp: {}", metricName, value, timestamp);

            try {
                return new MetricPOJO(metricName, Double.parseDouble(value), Long.valueOf(timestamp));
            }catch(Exception ex){
                log.error(ex.getMessage());
                log.info("Line without time request information - error by parsing time");
                return null;
            }
        }
        return null;
    }

}

