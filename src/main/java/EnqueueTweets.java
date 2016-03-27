import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

/**
 * Created by matt on 3/27/16.
 */
public class EnqueueTweets
{
    private static Logger logger = LoggerFactory.getLogger(EnqueueTweets.class);

    public static void main(String[] args) {
        try {
            Twitter twitter = TwitterFactory.getSingleton();
            TwitterReader reader = new TwitterReader(twitter);
            try (Producer<String, String> producer = new KafkaProducer<>(ImmutableMap.of(
                    "bootstrap.servers", "localhost:9092",
                    "key.serializer", "org.apache.kafka.common.serialization.StringSerializer",
                    "value.serializer", "org.apache.kafka.common.serialization.StringSerializer"
            ))) {
                for (String tweet : reader.getTweets("Trump")) {
                    producer.send(new ProducerRecord<>("test", "tweet", tweet));
                }
            }
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
