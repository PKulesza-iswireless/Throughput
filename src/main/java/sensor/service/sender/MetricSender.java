package sensor.service.sender;

import sensor.common.pojo.MetricPOJO;

public interface MetricSender {

    void sendMessage(MetricPOJO metricPOJO) throws Exception;

}

