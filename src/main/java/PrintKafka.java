import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import kafka.serializer.StringDecoder;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by matt on 3/27/16.
 */
public class PrintKafka
{
    private static Logger logger = LoggerFactory.getLogger(PrintKafka.class);

    // TODO make parameterizable.
    private static Integer distinctWordLimit = 10;
    private static double decayRate = 0.5;
    private static double dropThreshold = 0.25;
    private static double initialWeight = 1;

    public static void main(String[] args) {
        try (Producer<String, String> producer = new KafkaProducer<>(ImmutableMap.of(
                "bootstrap.servers", "localhost:9092",
                "key.serializer", "org.apache.kafka.common.serialization.StringSerializer",
                "value.serializer", "org.apache.kafka.common.serialization.StringSerializer"
        ))) {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Double> wordCounts = new HashMap<>();

            SparkConf conf = new SparkConf();
            conf.setAppName("PrintKafka");
            JavaStreamingContext context = new JavaStreamingContext(conf, Durations.seconds(2));
            JavaPairInputDStream<String, String> messages = KafkaUtils.createDirectStream(
                    context,
                    String.class,
                    String.class,
                    StringDecoder.class,
                    StringDecoder.class,
                    ImmutableMap.of("metadata.broker.list", "localhost:9092"),
                    ImmutableSet.of("test")
            );
            JavaDStream<String> values = messages.map(Tuple2::_2);
            values.foreach((rdd, t) -> {
                // Calculate rdd word counts
                Map<String, Long> batchWordCounts = rdd
                        .flatMap(tweet -> Arrays.asList(tweet.split("\\s+")))
                        .countByValue();
                logger.info("batch word counts {}", batchWordCounts);

                addWordCounts(wordCounts, batchWordCounts);
                List<Map.Entry<String, Double>> sortedWordCounts = wordCounts.entrySet().stream().sorted((entry1, entry2) -> {
                    return -Double.compare(entry1.getValue(), entry2.getValue());
                }).limit(distinctWordLimit).collect(Collectors.toList());
                logger.info("word counts {}", sortedWordCounts);

                Map<String, Double> sortedWordCountMap = sortedWordCounts.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                producer.send(new ProducerRecord<>("wordCounts", "wordCounts", objectMapper.writeValueAsString(sortedWordCountMap)));
                return null;
            });

            context.start();
            context.awaitTermination();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Implement time-based decay and weighting of word counts.
     * @param wordCounts
     * @param batchWordCounts
     */
    private static void addWordCounts (Map<String, Double> wordCounts, Map<String, Long> batchWordCounts) {

        // Perform decay. Don't decay if there are no new results.
        if (batchWordCounts.size() > 0) {
            for (Map.Entry<String, Double> existingCountEntry : wordCounts.entrySet()) {
                wordCounts.put(
                        existingCountEntry.getKey(),
                        existingCountEntry.getValue() * decayRate
                );
            }
        }

        // Increment weights.
        for (Map.Entry<String, Long> batchWordCountEntry : batchWordCounts.entrySet()) {
            Double weight = wordCounts.getOrDefault(batchWordCountEntry.getKey(), 0D);
            wordCounts.put(
                    batchWordCountEntry.getKey().toLowerCase(),  // Canonicalize terms.
                    weight + batchWordCountEntry.getValue() * initialWeight
            );
        }

        // Drop words below weight threshold.
        Map<String, Double> toDrop =
                wordCounts.entrySet()
                        .stream()
                        .filter(p -> p.getValue() <= dropThreshold)
                        .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

        for (String s : toDrop.keySet()) {
            wordCounts.remove(s);
        }
    }
}
