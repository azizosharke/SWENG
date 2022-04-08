package rapid7;

import rapid7.configuration.Configuration;
import rapid7.test_runner.TestRunner;

import javax.naming.TimeLimitExceededException;
import java.io.IOException;
import java.util.regex.Pattern;

public class Main {

    private static final String JSON_FILE_ENDING = ".json";
    private static final int MAX_ARGS = 2;
    public static final Pattern ILLEGAL_CHARS = Pattern.compile(".*[#<$+%>!`&*'\"|{}?=/:\\s@\\\\].*");

    public static void main(String[] args) throws IOException, TimeLimitExceededException, InterruptedException {
        final Integer configFileIndex = getConfigFileIndex(args);
        if (configFileIndex == null) return;

        run(new Configuration(args[configFileIndex]), args[1 - configFileIndex]);
    }

    private static Integer getConfigFileIndex(String[] args) {
        if (args.length > MAX_ARGS) {
            raiseError("There can only be two program arguments, being the configuration file in json format, and the version of the software being tested");
            return null;
        }

        if (args.length < MAX_ARGS) {
            raiseError("There must be two program arguments, being the configuration file in json format, and the version of the software being tested");
            return null;
        }

        final int configFileIndex = args[0].endsWith(JSON_FILE_ENDING) ? 0 : 1;
        final int versionIndex = 1 - configFileIndex;   // 1 - 0 -> 1, 1 - 1 -> 0. This means that 1 - configFileIndex will be the opposite index
        if (!args[configFileIndex].endsWith(JSON_FILE_ENDING)) {
            raiseError("One of the arguments must be the configuration file, in json format");
            return null;
        }

        if (JSON_FILE_ENDING.equals(args[configFileIndex])) {
            raiseError("%s is not a valid file name".formatted(JSON_FILE_ENDING));
            return null;
        }

        if (ILLEGAL_CHARS.matcher(args[versionIndex]).matches()) {
            raiseError("Version contains illegal character.\nIllegal characters:\t%s"
                    .formatted(ILLEGAL_CHARS.toString().replace(".*", "")
                            .replace("[", "").replace("]", "")));
            return null;
        }
        return configFileIndex;
    }

    public static void raiseError(String description) {
        System.err.println(description);
    }

    public static void run(Configuration configuration, String version) throws TimeLimitExceededException, InterruptedException, IOException {
        System.out.printf("Wrote results to %s%n", new TestRunner(configuration, version).performTests().writeResultsFile());
    }
}
