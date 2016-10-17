# AkkaKafkaSparkCassandra
Basic example for Akka, Kafka, Spark and Cassandra

This example shows how to integrate Akka, Kafka (via Akka Stream), Spark and Cassandra. In general, the example
1. Fits a linear regression mode with Mllib
2. Saves model
3. Sets up Akka Kafka stream
4. Based on input at Kafka topic predicts new value
5. Writes predicted value to Cassandra

It assumed that the software is installed:
Kafka_2.11-0.10.0.0
Apache Cassandra-3.7
Spark 2.0.0

# Run Example  
In order to run the example we need to do the following steps:

1) Open a new terminal to start Kafka Zookeeper
cd kafka
bin/zookeeper-server-start.sh config/zookeeper.properties

2) Open a new terminal to start Kafka Broker
cd Kafka_2.11
bin/kafka-server-start.sh config/server.properties

3) Open a new terminal to start Kafka Producer
cd Kafka_2.11
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic KafkaInput

4) Start Cassandra in a new terminal
sh cassandra/bin/cassandra

5) Start CQLSH in a new terminal and create a table
sh ~/cassandra/bin/cqlsh
CREATE KEYSPACE Spark_Cassandra WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1 };
CREATE TABLE Spark_Cassandra.words (word text PRIMARY KEY, count int);

6) Run sbt to build example 
cd AkkaKafkaSpark Cassandra
sbt assembly

7) Submit built jar file to Spark
$SPARK_HOME/bin/spark-submit   --class "Main"   --master local[4] target/scala-2.11/AkkaKafkaSparkCassandra-assembly-1.0.jar

8) Via the Kafka producer enter values in the format "2.0 4.0" to predict values via Mllib and write to Cassandra
