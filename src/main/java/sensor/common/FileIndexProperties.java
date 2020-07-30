package sensor.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.Properties;

import static sensor.common.FileIndexProperties.PropertyName.LAST_ANALYSED_FILE_PATH;
import static sensor.common.FileIndexProperties.PropertyName.LAST_ANALYSED_LINE_NUMBER;

@Slf4j
public class FileIndexProperties {

    private static final int DEFAULT_LINE_NUMBER = 1;
    private static FileIndexProperties INSTANCE = null;

    private Properties prop;
    private boolean isInitialized;
    private String pathToPropertiesFile;

    private FileIndexProperties(String pathToPropertiesFile){
        this.pathToPropertiesFile = pathToPropertiesFile;
        this.prop = new Properties();

        try {
            prop.load(new FileInputStream(pathToPropertiesFile));
            this.isInitialized = true;
        } catch (NullPointerException | IOException ex){
            log.warn("Could not load properties from {}, creating properties with default values", pathToPropertiesFile);
            try {
                createDefaultPropertiesFile(pathToPropertiesFile);
                this.isInitialized = true;
            }catch (IOException ioEx){
                log.error("Error by creating default properties file", ioEx);
            }
        }
    }

    public static void createInstance(String pathToLogsPropertiesFile){
        INSTANCE = new FileIndexProperties(pathToLogsPropertiesFile);
    }

    public static FileIndexProperties getInstance(){
        if (INSTANCE == null || !INSTANCE.isInitialized) throw new RuntimeException("Log tags properties not set");
        return INSTANCE;
    }

    public int getLastAnalysedLineNumber(){
        String value = this.prop.getProperty(LAST_ANALYSED_LINE_NUMBER.getName());
        return StringUtils.isNotBlank(value) ? Integer.parseInt(value) : DEFAULT_LINE_NUMBER;
    }

    public String getLastAnalysedFilePath(){
        return this.prop.getProperty(LAST_ANALYSED_FILE_PATH.getName());
    }

    public void setLastAnalysedLineNumber(int lastAnalysedLineNumber) {
        this.prop.setProperty(LAST_ANALYSED_LINE_NUMBER.getName(), String.valueOf(lastAnalysedLineNumber));
    }

    public void setLastAnalysedFilePath(String lastAnalysedFilePath){
        this.prop.setProperty(LAST_ANALYSED_FILE_PATH.getName(), lastAnalysedFilePath);
    }

    private void createDefaultPropertiesFile(String pathToMainPropertiesFile) throws IOException{
        log.info("Creating default properties file under default path:{}", pathToMainPropertiesFile);

        this.prop = new Properties();
        this.prop.setProperty(LAST_ANALYSED_LINE_NUMBER.getName(), String.valueOf(DEFAULT_LINE_NUMBER));
        this.prop.setProperty(LAST_ANALYSED_FILE_PATH.getName(),"");

        update();
    }

    public void update() throws IOException{
        File yourFile = new File(this.pathToPropertiesFile);
        yourFile.createNewFile(); // if file already exists will do nothing

        try(FileOutputStream out = new FileOutputStream(yourFile)) {
            this.prop.store(out, null);
            log.debug("Tag properties file updated, current values are: {{}: {}, {}: {})",
                    LAST_ANALYSED_FILE_PATH.getName(), getLastAnalysedFilePath(),
                    LAST_ANALYSED_LINE_NUMBER.getName(), getLastAnalysedLineNumber());
        }
    }

    @Getter
    @AllArgsConstructor
    enum PropertyName {

        LAST_ANALYSED_LINE_NUMBER("last.analysed.line.number"),

        LAST_ANALYSED_FILE_PATH("last.analysed.file.path");

        private String name;
    }

}

