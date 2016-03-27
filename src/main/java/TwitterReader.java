import com.google.common.base.Throwables;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by matt on 3/27/16.
 */
public class TwitterReader
{
    private Twitter twitter;

    public TwitterReader(Twitter twitter) {
        this.twitter = twitter;
    }

    public List<String> getTweets(String query) {
        try {
            QueryResult result = twitter.search().search(new Query(query));
            return result.getTweets().stream().map(Status::getText).collect(Collectors.toList());
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
