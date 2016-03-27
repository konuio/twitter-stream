Twitter Stream
===
```
# Start Zookeeper
zookeeper-server-start.sh /usr/local/etc/kafka/zookeeper.properties

# Start Kafka
kafka-server-start.sh /usr/local/etc/kafka/server.properties

# Start spark
SPARK_MASTER_IP=127.0.0.1 start-master.sh
start-slave.sh spark://localhost:7077

# Stop spark
stop-master.sh
stop-slave.sh

# Build and submit job
mvn package && spark-submit --class PrintKafka --master local[4] target/twitter-stream-1.0-SNAPSHOT-jar-with-dependencies.jar

# Add messages
kafka-console-producer.sh --broker-list localhost:9092 --topic test
```
