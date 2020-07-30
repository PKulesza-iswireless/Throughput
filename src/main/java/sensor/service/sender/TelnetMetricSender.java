package sensor.service.sender;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.telnet.TelnetClient;
import sensor.common.MetricProperties;
import sensor.common.pojo.MetricPOJO;
import sensor.service.converter.MetricConverter;

import java.io.PrintStream;

@Slf4j
@AllArgsConstructor
public class TelnetMetricSender implements MetricSender {

    private MetricProperties metricProperties;
    private MetricConverter metricConverter;

    @Override
    public void sendMessage(MetricPOJO metricPOJO) throws Exception {
        TelnetClient telnetClient = new TelnetClient();
        try {
            telnetClient.connect(metricProperties.getTelnetServerAddress(), metricProperties.getTelnetServerPort());

            String message = metricConverter.convert(metricPOJO);
            log.debug("Message: {}", message);
            PrintStream out = new PrintStream(telnetClient.getOutputStream());
            out.println(message);
            out.flush();
        } finally {
            telnetClient.disconnect();
        }
    }

}

