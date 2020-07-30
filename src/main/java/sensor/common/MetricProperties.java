package sensor.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import sensor.Arguments;
import sensor.common.enums.ApplicationName;
import sensor.common.enums.MetricSenderType;
import sensor.common.enums.MetricSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class MetricProperties {

    private static MetricProperties INSTANCE = null;

    private static Properties prop;
    private boolean isInitialized;

    private MetricProperties(String pathToPropertiesFile) throws IOException {
        readProperties(pathToPropertiesFile);

        MetricProperties.PropertiesValidator.ValidationResult validationResult = new MetricProperties.PropertiesValidator().validate();
        if (!validationResult.isValid()) {
            validationResult.getErrors().forEach((enumName, information) -> log.error("Property '{}' - {}", enumName.getName(), information));
            throw new IllegalArgumentException("Exception by parsing properties");
        }

        INSTANCE = this;
    }

    public static void createInstance(Arguments arguments) throws IOException {
        new MetricProperties(arguments.getPathToPropertiesFile());
    }

    public static MetricProperties getInstance() {
        if (INSTANCE == null || !INSTANCE.isInitialized) throw new RuntimeException("Properties not set");
        return INSTANCE;
    }

    private void readProperties(String pathToPropertiesFile) throws IOException {
        log.info("Loading properties from: {}", pathToPropertiesFile);

        try {
            prop = new Properties();
            prop.load(new FileInputStream(pathToPropertiesFile));
            this.isInitialized = true;
            log.info("Properties loaded");
        } catch (IOException ex) {
            log.error(ex.getMessage());
            throw ex;
        }
    }


    public String getMetricName() {
        return get(PropertyName.METRIC_NAME);
    }

    public String getMetricPattern() {
        return get(PropertyName.METRIC_PATTERN);
    }

    public int getCheckingTime() {
        return Integer.parseInt(getCheckingTimeAsString());
    }

    private String getCheckingTimeAsString() {
        return get(PropertyName.COLLECTOR_CHECKING_TIME);
    }

    public String getCollectorFileDir() {
        return get(PropertyName.COLLECTOR_FILE_DIR);
    }

    public String getCollectorFilePattern() {
        return get(PropertyName.COLLECTOR_FILE_PATTERN);
    }

    public String getCollectorFileIndex() {
        return get(PropertyName.COLLECTOR_FILE_INDEX);
    }

    public MetricSource getCollector() {
        return MetricSource.valueOf(getUppercase(PropertyName.COLLECTOR));
    }

    public ApplicationName getCollectorParser() {
        return ApplicationName.valueOf(getUppercase(PropertyName.COLLECTOR_PARSER));
    }

    public String getCollectorUrl() {
        return get(PropertyName.COLLECTOR_URL);
    }

    public String getCollectorUrlSuffixToGetAppId() {
        return get(PropertyName.COLLECTOR_URL_SUFFIX_GET_APP_ID);
    }

    public String getCollectorUrlSuffixToGetMetrics() {
        return get(PropertyName.COLLECTOR_URL_SUFFIX_GET_METRICS);
    }

    public int getCollectorUrlMaxDurationTime(){
        return Integer.valueOf(get(PropertyName.COLLECTOR_URL_MAX_DURATION_TIME));
    }

    public MetricSenderType getSenderType() {
        return MetricSenderType.valueOf(getUppercase(PropertyName.SENDER_TYPE));
    }

    public String getJmsServerAddress() {
        return get(PropertyName.JMS_SERVER_ADDRESS);
    }

    public int getJmsServerPort() {
        return Integer.valueOf(getJmsServerPortAsString());
    }

    private String getJmsServerPortAsString() {
        return get(PropertyName.JMS_SERVER_PORT);
    }

    public String getJmsServerUsername() {
        return get(PropertyName.JMS_SERVER_USERNAME);
    }

    public String getJmsServerPassword() {
        return get(PropertyName.JMS_SERVER_PASSWORD);
    }

    public String getTelnetServerAddress() {
        return get(PropertyName.TELNET_SERVER_ADDRESS);
    }

    public int getTelnetServerPort() {
        return Integer.valueOf(getTelnetServerPortAsString());
    }

    private String getTelnetServerPortAsString() {
        return get(PropertyName.TELNET_SERVER_PORT);
    }

    private String getUppercase(PropertyName propertyName) {
        return get(propertyName).toUpperCase().trim();
    }

    private String get(PropertyName propertyName) {
        return prop.getProperty(propertyName.getName());
    }

    @Getter
    @AllArgsConstructor
    enum PropertyName {

        COLLECTOR_CHECKING_TIME("collector.checking.time"),

        METRIC_NAME("metric.name"),

        METRIC_PATTERN("metric.pattern"),

        COLLECTOR("collector"),

        COLLECTOR_PARSER("collector.parser"),

        COLLECTOR_FILE_DIR("collector.file.dir"),
        COLLECTOR_FILE_PATTERN("collector.file.pattern"),
        COLLECTOR_FILE_INDEX("collector.file.index"),

        COLLECTOR_URL("collector.url"),
        COLLECTOR_URL_SUFFIX_GET_APP_ID("collector.url.suffix.get.app.id"),
        COLLECTOR_URL_SUFFIX_GET_METRICS("collector.url.suffix.get.metrics"),
        COLLECTOR_URL_MAX_DURATION_TIME("collector.url.max.duration.time"),

        SENDER_TYPE("sender.type"),

        JMS_SERVER_ADDRESS("jms.server.address"),
        JMS_SERVER_PORT("jms.sever.port"),
        JMS_SERVER_USERNAME("jms.server.username"),
        JMS_SERVER_PASSWORD("jms.server.password"),


        TELNET_SERVER_ADDRESS("telnet.server.address"),
        TELNET_SERVER_PORT("telnet.sever.port");

        private String name;

    }

    class PropertiesValidator {


        ValidationResult validate() {
            Map<PropertyName, String> errors = new HashMap<>();

            validateSender(errors);
            validateCollector(errors);
            validateIfBlank(errors, PropertyName.COLLECTOR_CHECKING_TIME, getCheckingTimeAsString());
            validateIfBlank(errors, PropertyName.METRIC_PATTERN, getMetricPattern());
            return new ValidationResult(errors);
        }

        private void validateCollector(Map<PropertyName, String> errors) {
            String metricSource = getUppercase(PropertyName.COLLECTOR);
            if (EnumUtils.isValidEnum(MetricSource.class, metricSource)) {
                MetricSource metricSourceEnum = EnumUtils.getEnum(MetricSource.class, metricSource);
                switch (metricSourceEnum) {

                    case URL:
                        String applicationName1 = getUppercase(PropertyName.COLLECTOR_PARSER);
                        if (EnumUtils.isValidEnum(ApplicationName.class, applicationName1)) {
                            ApplicationName applicationNameEnum = EnumUtils.getEnum(ApplicationName.class, applicationName1);
                            switch (applicationNameEnum) {
                                case GENOM:
                                    validateIfBlank(errors, PropertyName.COLLECTOR_URL, getCollectorUrl());
                                    validateIfBlank(errors, PropertyName.COLLECTOR_URL_SUFFIX_GET_APP_ID, getCollectorUrlSuffixToGetAppId());
                                    validateIfBlank(errors, PropertyName.COLLECTOR_URL_SUFFIX_GET_METRICS, getCollectorUrlSuffixToGetMetrics());
                                    validateIfBlank(errors, PropertyName.COLLECTOR_URL_MAX_DURATION_TIME, get(PropertyName.COLLECTOR_URL_MAX_DURATION_TIME));
                                    break;
                                default:
                                    errors.put(PropertyName.COLLECTOR_PARSER, String.format("Value: %s is currently not supported by metric generator for analyse from", applicationNameEnum));
                            }
                        }
                        break;

                    case FILE:
                        String applicationName = getUppercase(PropertyName.COLLECTOR_PARSER);
                        if (EnumUtils.isValidEnum(ApplicationName.class, applicationName)) {
                            ApplicationName applicationNameEnum = EnumUtils.getEnum(ApplicationName.class, applicationName);
                            switch (applicationNameEnum) {
                                case FCR:
                                case GENOM:
                                case CE_TRAFFIC:
                                    validateIfBlank(errors, PropertyName.COLLECTOR_FILE_DIR, getCollectorFileDir());
                                    validateIfBlank(errors, PropertyName.COLLECTOR_FILE_INDEX, getCollectorFileIndex());
                                    validateIfBlank(errors, PropertyName.COLLECTOR_FILE_PATTERN, getCollectorFilePattern());
                                    break;
                                default:
                                    errors.put(PropertyName.COLLECTOR_PARSER, String.format("Value: %s is currently not supported by metric generator", applicationNameEnum));
                            }
                            break;

                        } else {
                            errors.put(PropertyName.COLLECTOR_PARSER, String.format("Value %s is not supported, acceptable values: %s", applicationName, Arrays.toString(ApplicationName.values())));
                        }
                    default:
                        errors.put(PropertyName.SENDER_TYPE, String.format("Value: %s is currently not supported by metric generator", metricSourceEnum));

                }
            } else {
                errors.put(PropertyName.COLLECTOR, String.format("Value: %s is not supported, acceptable values: %s", metricSource, Arrays.toString(MetricSource.values())));

            }
        }

        private void validateSender(Map<PropertyName, String> errors) {
            String metricSenderType = getUppercase(PropertyName.SENDER_TYPE);
            if (EnumUtils.isValidEnum(MetricSenderType.class, metricSenderType)) {
                MetricSenderType metricSenderTypeEnum = EnumUtils.getEnum(MetricSenderType.class, metricSenderType);
                switch (metricSenderTypeEnum) {
                    case JMS:

                        validateIfBlank(errors, PropertyName.JMS_SERVER_ADDRESS, getJmsServerAddress());
                        validateIfBlank(errors, PropertyName.JMS_SERVER_PORT, getJmsServerPortAsString());
                        validateIfBlank(errors, PropertyName.JMS_SERVER_USERNAME, getJmsServerUsername());
                        validateIfBlank(errors, PropertyName.JMS_SERVER_PASSWORD, getJmsServerPassword());
                        break;
                    case TELNET:

                        validateIfBlank(errors, PropertyName.TELNET_SERVER_ADDRESS, getTelnetServerAddress());
                        validateIfBlank(errors, PropertyName.TELNET_SERVER_PORT, getTelnetServerPortAsString());
                        break;
                    default:
                        errors.put(PropertyName.SENDER_TYPE, String.format("Value: %s is currently not supported by metric generator", metricSenderTypeEnum));
                }
            } else {
                errors.put(PropertyName.SENDER_TYPE, String.format("Value: %s is not supported, acceptable values: %s", metricSenderType, Arrays.toString(MetricSource.values())));
            }
        }

        private void validateIfBlank(Map<PropertyName, String> errors, PropertyName propertyName, String value) {
            if (StringUtils.isBlank(value)) {
                errors.put(propertyName, "Value must be set");
            }
        }

        @Getter
        @AllArgsConstructor(access = AccessLevel.PRIVATE)
        class ValidationResult {
            private boolean valid;
            private Map<PropertyName, String> errors;

            ValidationResult(Map<PropertyName, String> errors) {
                this.valid = errors.isEmpty();
                this.errors = errors;
            }
        }
    }
}
