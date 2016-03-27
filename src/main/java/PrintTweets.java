import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 * Created by matt on 3/27/16.
 */
public class PrintTweets
{
    private static Logger logger = LoggerFactory.getLogger(PrintTweets.class);

    public static void main(String[] args) {
        try {
            Twitter twitter = TwitterFactory.getSingleton();
            QueryResult result = twitter.search().search(new Query("Trump"));
            for (Status status : result.getTweets()) {
                logger.info("tweet {}", status.getText());
            }
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
