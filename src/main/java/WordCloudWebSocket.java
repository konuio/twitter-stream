import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by matt on 4/10/16.
 */
public class WordCloudWebSocket extends WebSocketServer
{
    private static int WORD_CLOUD_PORT = 8083;
    private static Logger logger = LoggerFactory.getLogger(WordCloudWebSocket.class);
    private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public WordCloudWebSocket() throws UnknownHostException {
        super(new InetSocketAddress(WORD_CLOUD_PORT), DECODERS, null);
    }

    @Override
    public void onOpen (WebSocket conn, ClientHandshake handshake)
    {
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(ImmutableMap.of(
                "group.id", "wordCountWebSocket",
                "bootstrap.servers", "localhost:9092",
                "key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer",
                "value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer"
        ))) {
            logger.info("open");

            consumer.subscribe(Arrays.asList("wordCounts"));

            ScheduledFuture<?> task = executor.scheduleAtFixedRate(() -> {
                try {
                    logger.info("callback invoked");
                    ConsumerRecords<String, String> records = consumer.poll(0);
                    logger.info("found {} records", records.count());

                    for (ConsumerRecord<String, String> record : records) {
                        conn.send(record.value());
                    }
                }
                catch (Exception e) {
                    throw Throwables.propagate(e);
                }
            }, 0L, 1L, TimeUnit.SECONDS);
            task.get();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void onClose (WebSocket conn, int code, String reason, boolean remote)
    {
        logger.info("close");
    }

    @Override
    public void onMessage (WebSocket conn, String message)
    {
        logger.info("message {}", message);
    }

    @Override
    public void onError (WebSocket conn, Exception e)
    {
        logger.info("error", e);
    }

    /**
     * Just a standalone websocket server
     * @param args
     */
    public static void main(String[] args) {
        try {
            WordCloudWebSocket socket = new WordCloudWebSocket();
            socket.run();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
