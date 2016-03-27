import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Twitter;
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
            TwitterReader reader = new TwitterReader(twitter);
            for (String tweet : reader.getTweets("Trump")) {
                logger.info("tweet {}", tweet);
            }
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
