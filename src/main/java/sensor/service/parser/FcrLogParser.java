package sensor.service.parser;

import lombok.extern.slf4j.Slf4j;
import sensor.common.pojo.MetricPOJO;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
public class FcrLogParser implements LogParser {

    private static final String START_STATE_NAME = "poczatek";
    private static final String FINISH_STATE_NAME = "koniec";
    private static final int NUMBER_OF_LINE_PARTS = 4;
    private final static String BLANK_LINE_REGEX = "^\\s*$";

    private final static Pattern BLANK_LINE_PATTERN = Pattern.compile(BLANK_LINE_REGEX);

    private Map<String, Map<String, String>> fcrMethodsDataMap;
    private String metricName;

    public FcrLogParser(String metricName) {
        this.metricName = metricName;
        this.fcrMethodsDataMap = new HashMap<>();
    }

    @Override
    public MetricPOJO parseLineToPOJO(String line) {
        if (line == null || BLANK_LINE_PATTERN.matcher(line).find()) {
            log.info("line is blank");
            return null;
        }

        String[] lineParts = line.trim().split(":");

        if (lineParts.length != NUMBER_OF_LINE_PARTS) {
            log.info("Line: {} doesn't match pattern", line);
            return null;
        }

        String method = lineParts[0]; //wyslijPlik or usunPlik
        String state = lineParts[1]; //poczatek or koniec
        String timestamp = lineParts[2];
        String fileId = lineParts[3];
        log.info("Parsing line: method: {}, state:{}, timestamp:{}, fileId:{}", method, state, timestamp, fileId);
        if (START_STATE_NAME.equals(state)) {
            computeIfAbsent(method).put(fileId, timestamp);
        } else if (FINISH_STATE_NAME.equals(state)) {
            log.debug("Line with state {}, checking for state {}", FINISH_STATE_NAME, START_STATE_NAME);
            String startTimestamp = computeIfAbsent(method).get(fileId);
            if (startTimestamp != null) {
                log.debug("Found state {} with timestamp: {}", START_STATE_NAME, startTimestamp);
                double durationTime = Long.valueOf(timestamp) - Long.valueOf(startTimestamp);
                computeIfAbsent(method).remove(fileId);
                log.info("Prepare metric: metricName: {}, time: {}, timestamp: {}", metricName, durationTime, Long.valueOf(timestamp));
                return new MetricPOJO(metricName, durationTime, Long.valueOf(timestamp));
            }
        }
        return null;
    }

    private Map<String, String> computeIfAbsent(String method) {
        return fcrMethodsDataMap.computeIfAbsent(method, s -> new HashMap<>());
    }
}
