package rapid7.configuration;

import com.fasterxml.jackson.core.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public final class Configuration {
    private static final String SEARCH_FILE_STRING = "search_file";
    private static final String SEARCH_TEXT_STRING = "search_text";
    private static final String EXEC_COUNT_STRING = "execution_count";
    private static final String SERVER_URL_STRING = "server_url";
    private static final String POLLING_INTERVAL_STRING = "polling_interval";
    private static final String TIME_LIMIT_STRING = "time_limit";
    public static final int CHARACTER_LIMIT = 255;

    private static final long DEFAULT_POL_INT = 20;
    private static final long DEFAULT_TIME_LIMIT = 600000; // 10 minutes in ms
    public static final int DEFAULT_EXECUTION_COUNT = 1;

    private final String searchFile;
    private final String searchText;
    private final int executionCount;
    private final String serverUrl;
    private final long pollingInterval;
    private final long timeLimit;

    private record MediatorConfiguration(String searchFile, String searchText, int executionCount, String serverUrl,
                                         long pollingInterval, long timeLimit) {
    }

    public Configuration(InputStream inputStream) throws IOException {
        final MediatorConfiguration mConfig = getMediatorConfiguration(new JsonFactory().createParser(inputStream));

        this.searchFile = mConfig.searchFile();
        this.searchText = mConfig.searchText();
        this.executionCount = mConfig.executionCount();
        this.serverUrl = mConfig.serverUrl();
        this.pollingInterval = mConfig.pollingInterval();
        this.timeLimit = mConfig.timeLimit();
    }

    public Configuration(String fileName) throws IOException {
        final MediatorConfiguration mConfig = getMediatorConfiguration(new JsonFactory().createParser(
                new File(fileName)));

        this.searchFile = mConfig.searchFile();
        this.searchText = mConfig.searchText();
        this.executionCount = mConfig.executionCount();
        this.serverUrl = mConfig.serverUrl();
        this.pollingInterval = mConfig.pollingInterval();
        this.timeLimit = mConfig.timeLimit();
    }

    private MediatorConfiguration getMediatorConfiguration(JsonParser jsonParser) throws IOException {
        String searchFile = null;
        String searchText = null;
        int executionCount = DEFAULT_EXECUTION_COUNT;
        String serverUrl = null;
        long pollingInterval = DEFAULT_POL_INT;
        long timeLimit = DEFAULT_TIME_LIMIT;


        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            final String fieldName = jsonParser.getCurrentName();
            if (SEARCH_FILE_STRING.equalsIgnoreCase(fieldName)) {
                jsonParser.nextToken();
                searchFile = jsonParser.getText().trim();
            }

            if (SEARCH_TEXT_STRING.equalsIgnoreCase(fieldName)) {
                jsonParser.nextToken();
                searchText = jsonParser.getText().trim();
            }

            if (EXEC_COUNT_STRING.equalsIgnoreCase(fieldName)) {
                jsonParser.nextToken();
                executionCount = jsonParser.getIntValue();
            }

            if (SERVER_URL_STRING.equalsIgnoreCase(fieldName)) {
                jsonParser.nextToken();
                serverUrl = jsonParser.getText().trim();
            }

            if (POLLING_INTERVAL_STRING.equalsIgnoreCase(fieldName)) {
                jsonParser.nextToken();
                pollingInterval = jsonParser.getLongValue();
            }

            if (TIME_LIMIT_STRING.equalsIgnoreCase(fieldName)) {
                jsonParser.nextToken();
                timeLimit = jsonParser.getLongValue();
            }
        }
        jsonParser.close();

        checkProperties(searchFile, searchText, executionCount, serverUrl, pollingInterval, timeLimit);

        return new MediatorConfiguration(searchFile, searchText, executionCount, serverUrl, pollingInterval, timeLimit);
    }

    private void checkProperties(String searchFile, String searchText, long executionCount, String serverUrl,
                                 long pollingInterval, long timeLimit) {
        if (searchFile == null || "null".equals(searchFile)) {
            throw new RuntimeException(buildPropertyNotFoundMessage(SEARCH_FILE_STRING));
        }

        if (searchFile.length() > CHARACTER_LIMIT) {
            throw new RuntimeException("""
                    File name %s invalid.
                    File cannot be longer than %d characters""".formatted(searchFile, CHARACTER_LIMIT));
        }

        if (searchText == null || "null".equals(searchText)) {
            throw new RuntimeException(buildPropertyNotFoundMessage(SEARCH_TEXT_STRING));
        }

        if (executionCount <= 0) {
            throw new RuntimeException("%s cannot be negative or 0".formatted(EXEC_COUNT_STRING));
        }

        if (serverUrl == null || "null".equals(serverUrl)) {
            throw new RuntimeException(buildPropertyNotFoundMessage(SERVER_URL_STRING));
        }

        if (!serverUrl.startsWith("http")) {
            throw new RuntimeException("""
                    Server url %s invalid.
                    Must be a http server""".formatted(serverUrl));
        }

        if (pollingInterval <= 0) {
            throw new RuntimeException("%s cannot be negative or 0".formatted(POLLING_INTERVAL_STRING));
        }

        if (timeLimit <= 0) {
            throw new RuntimeException("%s cannot be negative or 0".formatted(TIME_LIMIT_STRING));
        }
    }

    private String buildPropertyNotFoundMessage(String property) {
        if (property == null) {
            throw new NullPointerException("property cannot be null");
        }

        return "%s property not found or null".formatted(property);
    }

    public String getSearchFile() {
        return searchFile;
    }

    public String getSearchText() {
        return searchText;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public long getPollingInterval() {
        return pollingInterval;
    }

    public long getTimeLimit() {
        return timeLimit;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Configuration) obj;
        return Objects.equals(this.searchFile, that.searchFile) &&
               Objects.equals(this.searchText, that.searchText) &&
               this.executionCount == that.executionCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(searchFile, searchText, executionCount);
    }

    @Override
    public String toString() {
        return "Configuration[searchFile=%s, searchText=%s, executionCount=%d, serverUrl=%s, pollingInterval=%d, timeLimit=%d]"
                .formatted(searchFile, searchText, executionCount, serverUrl, pollingInterval, timeLimit);
    }
}
