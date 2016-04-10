import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import kafka.serializer.StringDecoder;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.util.Arrays;
import java.util.Comparator;
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

    public static void main(String[] args) {
        try {
            Map<String, Long> wordCounts = new HashMap<>();
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
                List<Map.Entry<String, Long>> sortedWordCounts = wordCounts.entrySet().stream().sorted((entry1, entry2) -> {
                    return -Long.compare(entry1.getValue(), entry2.getValue());
                }).collect(Collectors.toList());
                logger.info("word counts {}", sortedWordCounts);
                return null;
            });

            context.start();
            context.awaitTermination();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private static void addWordCounts (Map<String, Long> wordCounts, Map<String, Long> batchWordCounts) {
        for (Map.Entry<String, Long> batchWordCountEntry : batchWordCounts.entrySet()) {
            wordCounts.put(
                    batchWordCountEntry.getKey(),
                    wordCounts.getOrDefault(batchWordCountEntry.getKey(), 0L) + batchWordCountEntry.getValue()
            );
        }
    }
}
