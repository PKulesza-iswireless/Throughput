package sensor.service.parser;

import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import sensor.common.pojo.MetricPOJO;

@Slf4j
public class ThroughputLogParser implements LogParser  {
	
	private static final int NUMBER_OF_LINE_PARTS = 2;
	private final static String BLANK_LINE_REGEX = "^\\s*$";
	private final static Pattern BLANK_LINE_PATTERN = Pattern.compile(BLANK_LINE_REGEX);
	
	private String metricName;
	
	public ThroughputLogParser(String metricName) {
		// TODO Auto-generated constructor stub
		this.metricName = metricName;
	}

	@Override
	public MetricPOJO parseLineToPOJO(String line) {
		// TODO Auto-generated method stub
		if (line == null || BLANK_LINE_PATTERN.matcher(line).find()) {
            log.info("line is blank");
            return null;
        }
		
		String[] lineParts = line.trim().split(":");
		
		 String timestamp = lineParts[0];
		 String metricValue = lineParts[1];

        if (lineParts.length != NUMBER_OF_LINE_PARTS) {
            log.info("Line: {} doesn't match pattern", line);
            return null;
        }
        
        double value = Double.parseDouble(metricValue);
        
		return new MetricPOJO(metricName, value, Long.valueOf(timestamp));
	}

}