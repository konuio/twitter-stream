Twitter Stream
===
Backend
---
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

# Produce messages
kafka-console-producer.sh --broker-list localhost:9092 --topic test

# Consume messages
kafka-console-consumer.sh --new-consumer --bootstrap-server localhost:9092 --topic test

# Topic admin
kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test
kafka-topics.sh --list --zookeeper localhost:2181
kafka-topics.sh --delete --zookeeper localhost:2181 --topic test
```

Frontend
---
```
cd frontend
npm install
npm start
# Visit http://localhost:8082
```
