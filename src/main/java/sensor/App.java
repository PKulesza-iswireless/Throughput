package sensor;

import java.util.Timer;

import lombok.extern.slf4j.Slf4j;
import sensor.common.FileIndexProperties;
import sensor.common.MetricProperties;
import sensor.cron.QuartzRunner;
import sensor.generatedata.SaveFileRunner;
import sensor.generatedata.SaveToFile;

@Slf4j
public class App {

    public static void main(String[] args) throws Exception{
        Arguments arguments = Arguments.fromMain(args);
        MetricProperties.createInstance(arguments);
        generateRandomData();
        sendMetricsFromLog();
        
    }

    private static void sendMetricsFromLog(){
        MetricProperties metricProperties = MetricProperties.getInstance();
        FileIndexProperties.createInstance(metricProperties.getCollectorFileIndex());

        new QuartzRunner().fireJob();
    }
    
    private static void generateRandomData() {
    	String filePath = "/home/ubuntu/Text.log";
    	SaveToFile saveToFile = new SaveToFile();
        saveToFile.creatFile(filePath);
        Timer timer = new Timer();
        timer.schedule(new SaveFileRunner(filePath), 0, 500);
    }
}

