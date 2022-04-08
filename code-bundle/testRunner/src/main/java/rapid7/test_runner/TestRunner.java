package rapid7.test_runner;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import rapid7.configuration.Configuration;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import javax.naming.TimeLimitExceededException;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TestRunner {
    private final Configuration configuration;
    private final String version;
    private final DescriptiveStatistics stats;

    public TestRunner(Configuration configuration, String version) {
        this.configuration = configuration;
        this.version = version;
        stats = new DescriptiveStatistics();
    }

    // TODO improve accuracy for timing
    private long getRequestRunnerMilliTime(RequestRunner requestRunner)
            throws InterruptedException, TimeLimitExceededException, IOException {
        final Instant start = Instant.now();
        requestRunner.runRequest(configuration.getSearchFile(), configuration.getSearchText());
        final Instant end = Instant.now();
        return Duration.between(start, end).toMillis();
    }

    /**
     * Performs tests according to the configuration that this class was passed when
     * instantiated
     * 
     * @return A reference to this object
     */
    public TestRunner performTests() throws InterruptedException, TimeLimitExceededException, IOException {
        if (stats.getN() <= 0 || stats.getN() < configuration.getExecutionCount()) {
            stats.clear(); // In the case that this function had been called but not finalised
            final RequestRunner requestRunner = new RequestRunner(
                    configuration.getServerUrl(), configuration.getPollingInterval(), configuration.getTimeLimit());

            final int executionLimit = configuration.getExecutionCount();
            for (int i = 0; i < executionLimit; i++) {
                stats.addValue(getRequestRunnerMilliTime(requestRunner));
            }
        }

        return this;
    }

    /**
     * This method is meant to be called after {@link #performTests()}
     * otherwise it will throw an UnsupportedOperationException.
     */
    public String writeResultsFile() throws IOException {
        if (stats.getN() <= 0) {
            throw new UnsupportedOperationException("The tests haven't been performed");
        }

        return writeResultsFile(new Results((long) stats.getElement(0),
                (long) stats.getElement((int) (stats.getN() - 1)),
                stats.getMean(), stats.getPercentile(99), stats.getPercentile(50)));
    }

    private record Results(@JsonProperty("first_result") long firstResult,
            @JsonProperty("final_result") long lastResult,
            @JsonProperty("average_run") double averageRun, @JsonProperty("P99") double p99,
            @JsonProperty("P50") double p50) {
    }

    private String writeResultsFile(Results results) throws IOException {
        final ObjectWriter writer = new ObjectMapper().writer(new DefaultPrettyPrinter());

        final File dir = new File("results");
        dir.mkdirs();
        final File file = new File(dir, "version-%s.json".formatted(this.version));
        file.createNewFile();
        writer.writeValue(file, results);
        return file.getPath();
    }
}
