package sensor.service.reader;

import com.jramoyo.io.IndexedFileReader;
import lombok.extern.slf4j.Slf4j;
import sensor.common.pojo.MetricPOJO;
import sensor.service.chooser.FileChooser;
import sensor.service.parser.LogParser;
import sensor.service.sender.MetricSender;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class FileMetricReader implements MetricReader {

    private LogParser logParser;
    private MetricSender metricSender;
    private FileChooser fileChooser;

    public FileMetricReader(LogParser logParser, MetricSender metricSender, FileChooser fileChooser) {
        this.logParser = Objects.requireNonNull(logParser, "LogParser could not be null");
        this.metricSender = Objects.requireNonNull(metricSender, "MetricSender could not be null");
        this.fileChooser = Objects.requireNonNull(fileChooser, "FileChooser could not be null");
    }

    @Override
    public void readMetrics() throws Exception {
        Map<Path, Integer> filesToAnalise = fileChooser.findFilesToAnalise();

        for (Path path : filesToAnalise.keySet()) {
            Integer lineNumber = filesToAnalise.get(path);
            IndexedFileReader reader = new IndexedFileReader(path.toFile());
            int linesCount = reader.getLineCount();

            log.info("Reading file: {} started", path.toString());

            for (Map.Entry<Integer, String> entry : reader.readLines(lineNumber, linesCount).entrySet()) {
                MetricPOJO metricPOJO = logParser.parseLineToPOJO(entry.getValue());
                if (metricPOJO != null) {
                    metricSender.sendMessage(metricPOJO);
                    fileChooser.updateFileIndex(path.toString(), entry.getKey());
                }
            }
            fileChooser.updateFileIndex(path.toString(), linesCount);

            log.info("Reading file: {} finished after reading {} lines ({} - {})", path.toString(), linesCount - lineNumber, lineNumber, linesCount);
        }
    }
}

