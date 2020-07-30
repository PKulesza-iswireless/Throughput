package sensor.service.chooser;

import lombok.extern.slf4j.Slf4j;
import sensor.common.FileIndexProperties;
import sensor.common.MetricProperties;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

@Slf4j
public class FcrFileChooser extends AbstractFileChooser {

    private final MetricProperties metricProperties;

    public FcrFileChooser(FileIndexProperties fileIndexProperties, MetricProperties metricProperties) {
        super(fileIndexProperties);
        this.metricProperties = metricProperties;
    }

    @Override
    protected List<Path> getFilesToAnalise() {
        String pathInString = metricProperties.getCollectorFileDir() + metricProperties.getCollectorFilePattern();
        log.info("Path to Fcr file with logs: {}", pathInString);
        Path path = Paths.get(pathInString);
        return Collections.singletonList(path);
    }
}

