import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.ConnectionLifeCycleListener;
import twitter4j.Status;
import twitter4j.StatusAdapter;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import java.util.concurrent.CountDownLatch;

/**
 * Created by matt on 3/27/16.
 */
public class ListenPush
{
    private static Logger logger = LoggerFactory.getLogger(ListenPush.class);

    public static void main(String[] args) {
        try {
            try (Producer<String, String> producer = new KafkaProducer<>(ImmutableMap.of(
                    "bootstrap.servers", "localhost:9092",
                    "key.serializer", "org.apache.kafka.common.serialization.StringSerializer",
                    "value.serializer", "org.apache.kafka.common.serialization.StringSerializer"
            ))) {
                StatusListener listener = new StatusAdapter()
                {
                    @Override
                    public void onStatus (Status status)
                    {
                       // logger.info("tweet {}", status.getText());
                        producer.send(new ProducerRecord<>("test", "tweet", status.getText()));
                    }
                };
                TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
                twitterStream.addListener(listener);
                twitterStream.filter("Trump");

                CountDownLatch disconnectLatch = new CountDownLatch(1);
                twitterStream.addConnectionLifeCycleListener(new ConnectionLifeCycleListener() {
                    @Override
                    public void onConnect ()
                    {
                    }

                    @Override
                    public void onDisconnect ()
                    {
                        disconnectLatch.countDown();
                    }

                    @Override
                    public void onCleanUp ()
                    {
                    }
                });
                disconnectLatch.await();
            }
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
