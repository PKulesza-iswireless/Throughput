package sensor.service.sender;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import sensor.common.MetricProperties;
import sensor.common.pojo.MetricPOJO;
import sensor.service.converter.MetricConverter;

import javax.jms.*;
import java.util.Objects;

@Slf4j
public class JmsMetricSender implements MetricSender {

    private MetricProperties metricProperties;
    private MetricConverter metricConverter;

    private ActiveMQConnectionFactory activeMQConnectionFactory;

    public JmsMetricSender(MetricProperties metricProperties, MetricConverter metricConverter) throws Exception {
        this.metricProperties = Objects.requireNonNull(metricProperties);
        this.metricConverter = Objects.requireNonNull(metricConverter);

        this.activeMQConnectionFactory = new ActiveMQConnectionFactory(metricProperties.getJmsServerUsername(), metricProperties.getJmsServerPassword(),
                metricProperties.getJmsServerAddress() + ":" + metricProperties.getJmsServerPort());
    }

    @Override
    public void sendMessage(MetricPOJO metricPOJO) throws Exception {
        Connection connection = startConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(metricPOJO.getMetricName());
        String message = metricConverter.convert(metricPOJO);
        log.info("Message: {}", message);
        Message msg = session.createTextMessage(message);
        MessageProducer producer = session.createProducer(topic);
        producer.send(msg);

        stopConnection(connection);
    }

    private Connection startConnection() throws JMSException {
        Connection connection = activeMQConnectionFactory.createConnection();
        connection.start();
        log.debug("Connection started");
        return connection;
    }

    private void stopConnection(Connection connection) throws Exception {
        if (connection != null) {
            connection.close();
        }
        log.debug("Connection closed");
    }

}

