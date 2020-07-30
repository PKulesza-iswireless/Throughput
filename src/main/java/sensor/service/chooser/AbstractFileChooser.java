package sensor.service.chooser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import sensor.common.FileIndexProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
public abstract class AbstractFileChooser implements FileChooser {

    @Getter(value = AccessLevel.PACKAGE)
    private final FileIndexProperties fileIndexProperties;

    @Override
    public Map<Path, Integer> findFilesToAnalise() throws IOException {
        List<Path> filesToAnalise = getFilesToAnalise();

        String lastAnalysedFilePath = fileIndexProperties.getLastAnalysedFilePath();
        Map<Path, Integer> result = new HashMap<>();
        for (Path path : filesToAnalise) {
            if (lastAnalysedFilePath != null && path.toString().equals(lastAnalysedFilePath)) {
                //check line numbers
                if (Files.lines(path).count() > fileIndexProperties.getLastAnalysedLineNumber()) {
                    result.put(path, fileIndexProperties.getLastAnalysedLineNumber() );
                } else {
                    log.debug("Skiping {} - file is currently read", path.getFileName().toString());
                }
            } else {
                result.put(path, 1);
            }
        }

        log.info("Files to analise: {}", result.keySet().size());
        result.forEach((path, line) -> log.info("\t {} from line: {}", path.getFileName().toString(), line));
        return result;
    }

    protected abstract List<Path> getFilesToAnalise() throws IOException;

    @Override
    public void updateFileIndex(String filePath, Integer line) throws IOException {
        fileIndexProperties.setLastAnalysedFilePath(filePath);
        fileIndexProperties.setLastAnalysedLineNumber(line);
        fileIndexProperties.update();
    }
}

