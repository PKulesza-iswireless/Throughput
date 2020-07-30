package sensor.service.reader;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import sensor.common.MetricProperties;
import sensor.common.pojo.MetricPOJO;
import sensor.service.sender.MetricSender;

import javax.ws.rs.core.UriBuilder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GenomUrlMetricReader extends UrlMetricReader {

    private MetricProperties metricProperties;
    private MetricSender metricSender;
    private String basicUrl;

    private static final String LEFT_TASKS_METRIC_NAME = "SimulationLeftNumber";
    private static final String TIME_PERCENTILE_METRIC_NAME = "ETPercentile";
    private static final String REMAINING_TIME_METRIC_NAME = "RemainingSimulationTimeMetric";
    private static final String ELAPSED_TIME_METRIC_NAME = "SimulationElapsedTime";
    private static final String TOTAL_CORES_METRIC_NAME = "TotalCores";

    private static final String GETTING_JOBS_URL_SUFFIX = "/stages/1";
    private static final String GETTING_EXECUTORS_URL_SUFFIX = "/executors";


    public GenomUrlMetricReader(MetricSender metricSender, MetricProperties metricProperties) {
        this.metricSender = Objects.requireNonNull(metricSender, "MetricSender could not be null");
        this.metricProperties = Objects.requireNonNull(metricProperties, "MetricProperties could not be null");
        this.basicUrl = metricProperties.getCollectorUrl();
    }

    @Override
    public void readMetrics() throws Exception {

        log.info("Basic url: {}", basicUrl);

        String applicationId = findApplicationId();

        if (applicationId == null) {
            log.error("Error by getting applicationId");
            return;
        }
        log.info("Application id: {}", applicationId);

        MeasurementValues lastMeasurementValues = null;
        try {

            MeasurementValues currentMeasurementValues = findExecutingJobsInfo(applicationId);

            int numLeftTasks = currentMeasurementValues.getNumTasks() + currentMeasurementValues.getNumFailedTasks()
                    + currentMeasurementValues.getNumKilledTasks() - currentMeasurementValues.getNumCompletedTasks();
            long elapsedTimeInSeconds = currentMeasurementValues.getElapsedTimeInSeconds();
            long remainingTimeInSeconds = currentMeasurementValues.getRemainingTimeInSeconds();
            double percentileTaskTime = currentMeasurementValues.getPercentileTaskTime();

            int totalCores = currentMeasurementValues.getTotalCores();


            log.info("allTasksNumber: {}, numCompletedTasks: {}, leftTasksNumber: {}, remainingTime: {}s, p95: {}s",
                    currentMeasurementValues.getNumTasks(), currentMeasurementValues.getNumCompletedTasks(),
                    numLeftTasks, remainingTimeInSeconds, percentileTaskTime);

            lastMeasurementValues = currentMeasurementValues;

            createAndSendGenomMetrics(numLeftTasks, percentileTaskTime, elapsedTimeInSeconds, remainingTimeInSeconds, totalCores);

        } catch (ClientHandlerException ex) {
            if (lastMeasurementValues != null) {
                log.info("Job closed, {} tasks from {} completed per 100%", lastMeasurementValues.getNumCompletedTasks(), lastMeasurementValues.getNumTasks());
            }
            return;
        }
    }

    private void createAndSendGenomMetrics(int numLeftTasks, Double percentileTaskTime, long elapsedTimeInSeconds, long durationTime, int totalCores) throws Exception {
        long currentTimestamp = getTimestamp();
        metricSender.sendMessage(new MetricPOJO(LEFT_TASKS_METRIC_NAME, numLeftTasks, currentTimestamp));
        metricSender.sendMessage(new MetricPOJO(TIME_PERCENTILE_METRIC_NAME, percentileTaskTime, currentTimestamp));
        metricSender.sendMessage(new MetricPOJO(ELAPSED_TIME_METRIC_NAME, elapsedTimeInSeconds, currentTimestamp));
        metricSender.sendMessage(new MetricPOJO(REMAINING_TIME_METRIC_NAME, durationTime, currentTimestamp));
        metricSender.sendMessage(new MetricPOJO(TOTAL_CORES_METRIC_NAME, totalCores, currentTimestamp));
    }

    private String findApplicationId() throws RuntimeException {
        String urlToGetAppId = basicUrl + metricProperties.getCollectorUrlSuffixToGetAppId();
        log.info("Connection to url: {}", urlToGetAppId);

        JSONArray jsonArray = new JSONArray(getDataFromUrl(urlToGetAppId));

        return convert(jsonArray).stream()
                .findFirst()
                .map(jsonObject -> jsonObject.getString("id"))
                .orElseThrow(() -> new RuntimeException("Error by parsing JSON response with application id"));
    }

    private List<JSONObject> convert(JSONArray jsonArray) {
        List<JSONObject> result = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            result.add(jsonArray.getJSONObject(i));
        }
        return result;
    }


    private MeasurementValues findExecutingJobsInfo(String applicationId) throws ParseException {
        String urlToGetJobsInfo = basicUrl + metricProperties.getCollectorUrlSuffixToGetAppId() + "/" + applicationId + GETTING_JOBS_URL_SUFFIX;
        log.info("Get jobs statistics from url: {}", urlToGetJobsInfo);

        String jobsInfo = getDataFromUrl(urlToGetJobsInfo);

        JSONArray jsonArray = new JSONArray(jobsInfo);

        log.info("All jobs number: {}", jsonArray.length());
        JSONObject jsonObject = jsonArray.getJSONObject(0);

        int numTask = jsonObject.getInt("numTasks");
        int numCompletedTasks = jsonObject.getInt("numCompleteTasks");
        int numFailedTasks = jsonObject.getInt("numFailedTasks");
        int numActiveTasks = jsonObject.getInt("numActiveTasks");
        int numKilledTasks = jsonObject.getInt("numKilledTasks");

        Date startTime = parseDate(jsonObject.getString("submissionTime"));
        Pair<Long, Long> elapsedRemainingTime = countElapsedRemainingTime(startTime, new Date());
        Long simulationElapsedTime = elapsedRemainingTime.getLeft();
        Long remainingInSeconds = elapsedRemainingTime.getRight();

        ExecutorsMeasurementValues executorsMeasurementValues = findTotalCoresAndMeanDurationTime(applicationId);
        int totalCores = executorsMeasurementValues.getTotalCores();
        long percentileTaskTime = executorsMeasurementValues.getMeanDurationTime();

        log.info("Job information: \n numTasks: {}, \n numCompletedTasks: {}, \n numFailedTasks: {}," +
                        "\n numActiveTasks: {}, \n numKilledTasks: {}, \n startTime: {}, " +
                        " \n simulationElapsedTime= {}, \n remainingInSeconds= {}, \n totalCores= {}, \n percentileTaskTime= {}", numTask, numCompletedTasks,
                numFailedTasks, numActiveTasks, numKilledTasks, startTime, simulationElapsedTime, remainingInSeconds, totalCores, percentileTaskTime);

        return new MeasurementValues(numTask, numCompletedTasks, numFailedTasks, numActiveTasks, numKilledTasks, simulationElapsedTime, remainingInSeconds, totalCores, percentileTaskTime);

    }

    private ExecutorsMeasurementValues findTotalCoresAndMeanDurationTime(String applicationId) {
        String urlToGetExecutorsInfo = basicUrl + metricProperties.getCollectorUrlSuffixToGetAppId() + "/" + applicationId + GETTING_EXECUTORS_URL_SUFFIX;
        log.info("Checking executors info from: {}", urlToGetExecutorsInfo);
        String response = getDataFromUrl(urlToGetExecutorsInfo);

        JSONArray jsonArray = new JSONArray(response);

        int executorsNumber = jsonArray.length() - 1; //omit driver0
        int totalCoresSum = 0;
        long totalMeanDurationTimes = 0;

        for (int i = 1; i < jsonArray.length(); i++) { //start at 1, omit driver0
            Integer totalCoresPerExecutor = (Integer) jsonArray.getJSONObject(i).get("totalCores");
            totalCoresSum += totalCoresPerExecutor;

            int totalDurationPerExecutor = (Integer) jsonArray.getJSONObject(i).get("totalDuration");
            int completedTasksPerExecutor = (Integer) jsonArray.getJSONObject(i).get("completedTasks");

            if (completedTasksPerExecutor == 0) { //executor did not performed any tasks
                executorsNumber--;
            } else {
                long meanDurationTimePerExecutorInS = totalDurationPerExecutor / (1000 * completedTasksPerExecutor);
                totalMeanDurationTimes += meanDurationTimePerExecutorInS;
            }
        }

        long meanDurationTime = totalMeanDurationTimes/executorsNumber;
        return new ExecutorsMeasurementValues(totalCoresSum, meanDurationTime);
    }

    private Pair<Long, Long> countElapsedRemainingTime(Date startDate, Date endDate) {
        long diffInS = TimeUnit.MILLISECONDS.toSeconds(endDate.getTime() - startDate.getTime());
        long maxDurationTimeInS = metricProperties.getCollectorUrlMaxDurationTime();
        long remainingTime = maxDurationTimeInS - diffInS;
        return Pair.of(diffInS, remainingTime);
    }


    private String getDataFromUrl(String url) {
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);

        WebResource webResource = client.resource(UriBuilder.fromUri(url).build());
        ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

        String result = "";
        if (response.getStatus() == 200) {

            result = response.getEntity(String.class);
            log.debug("Response entity from url:{}   : {}", url, result);
        }
        return result;
    }

    private long getTimestamp() {
        return new Date().getTime();
    }

    private Date parseDate(String dateStr) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(dateStr);
    }

    @AllArgsConstructor
    @Getter
    class MeasurementValues {

        Integer numTasks;
        Integer numCompletedTasks;
        Integer numFailedTasks;
        Integer numActiveTasks;
        Integer numKilledTasks;
        Long elapsedTimeInSeconds;
        Long remainingTimeInSeconds;
        Integer totalCores;
        Long percentileTaskTime;
    }

    @AllArgsConstructor
    @Getter
    class ExecutorsMeasurementValues{

        Integer totalCores;
        Long meanDurationTime;
    }
}

