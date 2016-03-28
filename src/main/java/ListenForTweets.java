import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.StatusAdapter;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

/**
 * Created by matt on 3/27/16.
 */
public class ListenForTweets
{
    private static Logger logger = LoggerFactory.getLogger(ListenForTweets.class);

    public static void main(String[] args) {
        try {
            StatusListener listener = new StatusAdapter()
            {
                @Override
                public void onStatus (Status status)
                {
                    logger.info("tweet {}", status.getText());
                }
            };
            TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
            twitterStream.addListener(listener);
            twitterStream.filter("Trump");

            // remove listener after we no longer want new events
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
