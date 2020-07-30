package sensor.cron;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import sensor.common.Manager;
import sensor.service.reader.MetricReader;

import java.util.concurrent.CountDownLatch;

@Slf4j
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class MetricGeneratorJob implements Job {

    private static int count;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        log.info("--------------------------------------------------------------------");
        log.info("MyJob start: {}", jobExecutionContext.getFireTime());
        log.info("MyJob end: {}, key: {}", jobExecutionContext.getJobRunTime(), jobDetail.getKey());
        log.info("MyJob next scheduled time: {}", jobExecutionContext.getNextFireTime());
        log.info("--------------------------------------------------------------------");

        executeSending();

        ((CountDownLatch) jobDetail.getJobDataMap().get("latch")).countDown();

        count++;
        log.info("Job count {}", count);
    }

    private void executeSending(){
        MetricReader properMetricReader = Manager.getProperMetricReader();
        try {
            properMetricReader.readMetrics();
        } catch (Exception e){
            log.error("Error during reading metrics", e);
        }
    }
}
