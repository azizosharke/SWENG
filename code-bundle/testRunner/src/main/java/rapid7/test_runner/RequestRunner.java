package rapid7.test_runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.fluent.Content;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;

import javax.naming.TimeLimitExceededException;
import java.io.IOException;

public record RequestRunner(String url, long pollingInterval, long timeLimitInMs) {

    private String doPost(String searchFile, String searchText) throws IOException {
        final Timeout timeout = Timeout.ofMilliseconds(timeLimitInMs);
        return getSearchId(
                Request.post(url)
                        .connectTimeout(timeout)
                        .responseTimeout(timeout)
                .body(new StringEntity("{\"search_file\":\"%s\",\"search_text\":\"%s\"}"
                        .formatted(searchFile, searchText),
                        ContentType.APPLICATION_JSON))
                .execute()
                .returnContent());
    }

    private String getSearchId(Content content) throws IOException {
        return new ObjectMapper().readTree(content.asString()).get("search_id").asText();
    }

    private boolean doGet(String searchID) throws IOException {
        final Timeout timeout = Timeout.ofMilliseconds(timeLimitInMs);
        return Request.get("%s%s%s".formatted(url, url.endsWith("/") ? "" : "/", searchID))
                .connectTimeout(timeout)
                .responseTimeout(timeout)
                .execute()
                .returnContent()
                .asString()
                .contains("done");
    }

    public void runRequest(String searchFile, String searchText)
            throws InterruptedException, TimeLimitExceededException, IOException {
        final long startingTime = System.currentTimeMillis();
        final String searchID = doPost(searchFile, searchText);
        while (!doGet(searchID)) {
            Thread.sleep(pollingInterval);
            if (System.currentTimeMillis() - startingTime >= timeLimitInMs) {
                throw new TimeLimitExceededException("Request has been busy waiting for over %d milliseconds".
                        formatted(timeLimitInMs));
            }
        } // Run until request has been finalised
    }
}
