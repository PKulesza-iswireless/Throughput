package sensor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

@Getter
@Setter
@Slf4j
@NoArgsConstructor
public class Arguments {

    @Option(name = "-p", usage = "path to properties file", required = true)
    private String pathToPropertiesFile = null;

    public static Arguments fromMain(String[] args) throws CmdLineException {
        return read(args);
    }

    private static Arguments read(String[] args) throws CmdLineException {
        Arguments arguments = new Arguments();
        CmdLineParser parser = new CmdLineParser(arguments);
        try {
            parser.parseArgument(args);
            return arguments;
        } catch (CmdLineException e){
            log.error(e.getMessage());
            parser.printUsage(System.err);
            throw e;
        }
    }
}
