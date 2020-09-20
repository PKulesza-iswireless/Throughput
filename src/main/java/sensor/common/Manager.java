package sensor.common;

import lombok.extern.slf4j.Slf4j;
import sensor.common.enums.ApplicationName;
import sensor.common.enums.MetricSenderType;
import sensor.common.enums.MetricSource;
import sensor.service.chooser.CeTrafficFileChooser;
import sensor.service.chooser.FileChooser;
import sensor.service.chooser.ThroughputFileChooser;
import sensor.service.chooser.FcrFileChooser;
import sensor.service.converter.MetricConverter;
import sensor.service.converter.PlaceholderMetricConverter;
import sensor.service.parser.CeTrafficLogParser;
import sensor.service.parser.FcrLogParser;
import sensor.service.parser.GenomLogParser;
import sensor.service.parser.ThroughputLogParser;
import sensor.service.parser.LogParser;
import sensor.service.parser.TestLogParser;
import sensor.service.reader.FileMetricReader;
import sensor.service.reader.GenomUrlMetricReader;
import sensor.service.reader.MetricReader;
import sensor.service.reader.UrlMetricReader;
import sensor.service.sender.MetricSender;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Manager {

    private static final MetricProperties metricProperties = MetricProperties.getInstance();
    private static final FileIndexProperties fileIndexProperties = FileIndexProperties.getInstance();

    private static final Map<String, SingletonSupplier> suppliersContext = new HashMap<>();


    private static LogParser getProperLogParser() {
        return createProperLogParser(metricProperties.getCollectorParser());
    }

    private static LogParser createProperLogParser(ApplicationName applicationName) {

        return (LogParser) suppliersContext.computeIfAbsent("LogParser", s -> new SingletonSupplier<>(() -> {
            switch (applicationName) {
            	case THROUGHPUT:
            		return new ThroughputLogParser(metricProperties.getMetricName());
                case FCR:
                    return new FcrLogParser(metricProperties.getMetricName());
                case GENOM:
                    return new GenomLogParser(metricProperties.getMetricName());
                case TEST:
                	return new TestLogParser(metricProperties.getMetricName());
                case CE_TRAFFIC:
                    return new CeTrafficLogParser();
                default:
                    throw new RuntimeException("There is no LogParser instance for " + applicationName);
            }
        })).get();

    }

    private static MetricConverter getMetricConverter() {
        return new PlaceholderMetricConverter(metricProperties.getMetricPattern());
    }

    public static MetricSender getProperNewMetricSender() {
        return createNewMetricSender(metricProperties.getSenderType());
    }

    private static MetricSender createNewMetricSender(MetricSenderType metricSenderType) {

        return (MetricSender) suppliersContext.computeIfAbsent("MetricSender", s -> new SingletonSupplier<>(() -> {
            switch (metricSenderType) {
                case JMS:
                    try {
                        return new sensor.service.sender.JmsMetricSender(metricProperties, getMetricConverter());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                case TELNET:
                    return new sensor.service.sender.TelnetMetricSender(metricProperties, getMetricConverter());
                default:
                    throw new RuntimeException("There is no MetricSender instance for " + metricSenderType);
            }
        })).get();
    }


    public static MetricReader getProperMetricReader() {
        return createMetricReader(metricProperties.getCollector());
    }

    private static MetricReader createMetricReader(MetricSource metricSource) {

        return (MetricReader) suppliersContext.computeIfAbsent("MetricReader", s -> new SingletonSupplier<>(() -> {
            switch (metricSource) {
                case FILE:
                    return new FileMetricReader(getProperLogParser(), getProperNewMetricSender(), getFileChooser());
                case URL:
                    return getProperUrlMetricReader();
                default:
                    throw new RuntimeException("There is no MetricReader instance for " + metricSource);
            }
        })).get();
    }

    private static UrlMetricReader getProperUrlMetricReader() {
        return createUrlMetricReader(metricProperties.getCollectorParser());
    }

    private static UrlMetricReader createUrlMetricReader(ApplicationName applicationName) {
        return (UrlMetricReader) suppliersContext.computeIfAbsent("UrlMetricReader", s -> new SingletonSupplier<>(() -> {
            switch (applicationName) {
                case GENOM:
                    return new GenomUrlMetricReader(getProperNewMetricSender(), metricProperties);
                default:
                    throw new RuntimeException("There is no UrlMetricReader instance for " + applicationName);
            }
        })).get();
    }


    private static FileChooser getFileChooser() {
        return createFileChooser(metricProperties.getCollectorParser());
    }

    private static FileChooser createFileChooser(ApplicationName applicationName) {
        return (FileChooser) suppliersContext.computeIfAbsent("FileChooser", s -> new SingletonSupplier<>(() -> {
            switch (applicationName) {
            	case THROUGHPUT:
            		return new ThroughputFileChooser(fileIndexProperties, metricProperties);
            	case TEST:
                case FCR:
                    return new FcrFileChooser(fileIndexProperties, metricProperties);
                case CE_TRAFFIC:
                    return new CeTrafficFileChooser(metricProperties, fileIndexProperties);
                default:
                    throw new RuntimeException("There is no FileChooser instance for " + applicationName);
            }
        })).get();
    }
}
