package sensor.service.chooser;

import lombok.extern.slf4j.Slf4j;
import sensor.common.FileIndexProperties;
import sensor.common.MetricProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class CeTrafficFileChooser extends AbstractFileChooser {

    private final MetricProperties metricProperties;
    private final Predicate<Path> fileNamePredicate;

    public CeTrafficFileChooser(MetricProperties metricProperties, FileIndexProperties fileIndexProperties) {
        super(fileIndexProperties);
        this.metricProperties = metricProperties;
        this.fileNamePredicate = path -> path.getFileName().toString().equalsIgnoreCase(metricProperties.getCollectorFilePattern());
    }

    @Override
    protected List<Path> getFilesToAnalise() throws IOException {
        return Files
                .list(Paths.get(metricProperties.getCollectorFileDir()))
                .filter(fileNamePredicate)
                .sorted()
                .collect(Collectors.toList());
    }
}
