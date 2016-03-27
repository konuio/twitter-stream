import com.google.common.base.Throwables;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by matt on 3/27/16.
 */
public class PrintLines
{
    private static Logger logger = LoggerFactory.getLogger(PrintLines.class);

    public static void main(String[] args) {
        try {
            SparkConf conf = new SparkConf();
            conf.setAppName("PrintLines");
            JavaSparkContext context = new JavaSparkContext(conf);
            JavaRDD<String> lines = context.textFile("input.txt").cache();
            lines.foreach(line -> {
                logger.info("line: {}", line);
            });
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
