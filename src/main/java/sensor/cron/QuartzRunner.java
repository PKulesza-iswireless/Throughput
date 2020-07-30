package sensor.cron;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import sensor.common.Manager;
import sensor.common.MetricProperties;

import java.util.concurrent.CountDownLatch;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@Getter
@Slf4j
@NoArgsConstructor
public class QuartzRunner {

    private MetricProperties metricProperties = MetricProperties.getInstance();
    private CountDownLatch latch = new CountDownLatch(1);

    public void fireJob() {
        try {
            log.info("Metric Sender: {}", Manager.getProperNewMetricSender().getClass().toString());
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.scheduleJob(getJobDetail(), getTrigger(metricProperties.getCheckingTime()));
            scheduler.start();

            latch.await();
        } catch (SchedulerException | InterruptedException ex) {
            log.error(ex.getMessage());
        }
    }

    private JobDetail getJobDetail() {
        JobDataMap data = new JobDataMap();
        data.put("latch", this.latch);

        return JobBuilder.newJob(MetricGeneratorJob.class)
                .usingJobData(data)
                .withIdentity("myJob", "group1")
                .build();
    }

    private Trigger getTrigger(int secondsBetweenCronStarts) {
        // Trigger the job to run now, and then every secondsBetweenCronStarts seconds
        return newTrigger()
                .withIdentity("myTrigger", "group1")
                .startNow()
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(secondsBetweenCronStarts)
                        .repeatForever())
                .build();
    }

}

