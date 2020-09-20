package sensor;

import java.util.Timer;

import lombok.extern.slf4j.Slf4j;
import sensor.common.FileIndexProperties;
import sensor.common.MetricProperties;
import sensor.cron.QuartzRunner;
@Slf4j
public class App {

    public static void main(String[] args) throws Exception{
        Arguments arguments = Arguments.fromMain(args);
        MetricProperties.createInstance(arguments);
        sendMetricsFromLog();
        
    }

    private static void sendMetricsFromLog(){
        MetricProperties metricProperties = MetricProperties.getInstance();
        FileIndexProperties.createInstance(metricProperties.getCollectorFileIndex());

        new QuartzRunner().fireJob();
    }
    
   
}

