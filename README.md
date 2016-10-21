# AkkaKafkaSparkCassandra
Basic example for Akka, Kafka, Spark and Cassandra

This example shows how to integrate Akka, Kafka (via Akka Stream), Spark and Cassandra in combination with Mllib. In general, the example<br />
1. Fits a linear regression mode with Mllib<br />
2. Saves model<br />
3. Sets up Akka Kafka stream<br />
4. Based on input at Kafka topic predicts new value<br />
5. Writes predicted value to Cassandra<br />

It assumed that the software is installed:<br />
Kafka_2.11-0.10.0.0<br />
Apache Cassandra-3.7<br />
Spark 2.0.0<br />

# Run Example
In order to run the example we need to do the following steps:<br />

1) Open a new terminal to start Kafka Zookeeper<br />
cd kafka<br/>
bin/zookeeper-server-start.sh config/zookeeper.properties<br />

2) Open a new terminal to start Kafka Broker<br />
cd Kafka_2.11<br />
bin/kafka-server-start.sh config/server.properties<br />

3) Open a new terminal to start Kafka Producer<br />
cd Kafka_2.11<br />
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic KafkaInput<br />

4) Start Cassandra in a new terminal<br />
sh cassandra/bin/cassandra<br />

5) Start CQLSH in a new terminal and create a table<br />
sh ~/cassandra/bin/cqlsh <br />
CREATE KEYSPACE Spark_Cassandra WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor':1};  <br />
CREATE TABLE Spark_Cassandra.words (word text PRIMARY KEY, count int);<br />

6) Run sbt to build example <br />
cd AkkaKafkaSpark Cassandra<br />
sbt assembly<br />

7) Submit built jar file to Spark<br />
$SPARK_HOME/bin/spark-submit   --class "Main"   --master local[4] target/scala-2.11/AkkaKafkaSparkCassandra-assembly-1.0.jar<br />

8) Via the Kafka producer enter values in the format "2.0 4.0" to predict values via Mllib and write to Cassandra<br />
