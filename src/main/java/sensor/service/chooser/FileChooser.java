package sensor.service.chooser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public interface FileChooser {

    Map<Path, Integer> findFilesToAnalise() throws IOException ;

    void updateFileIndex(String filePath, Integer line) throws IOException;
}
