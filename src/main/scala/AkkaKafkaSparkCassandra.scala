
import java.time.Instant
import java.nio.file.{Paths, Files}

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import com.datastax.spark.connector._
import com.datastax.spark.connector.rdd._
import org.apache.spark.sql.cassandra._

import org.apache.spark.sql.SQLContext
import com.datastax.spark.connector.cql.CassandraConnector._

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.regression.LinearRegressionModel
import org.apache.spark.mllib.regression.LinearRegressionWithSGD

object Main {

  def main(args: Array[String]): Unit = {
    // 1. Fit linear regression with Mllib
    // 2. Save model
    // 3. Setup Akka Kafka stream
    // 4. Based on input at Kafka topic predict new value
    // 5. Write predicted value to Cassandra
     
    val conf = new SparkConf().setAppName("AkkaKafkaSparkCassandra")
    conf.set("spark.cassandra.connection.host", "127.0.0.1") 
    val sc = new SparkContext(conf)

    //Fit linear Regression
    // Load and parse the data
    val data = sc.textFile("data/regression.data")
    val parsedData = data.map { line =>
      val parts = line.split(',')
      LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble)))
    }.cache()

    // Building the model
    val numIterations = 100
    val stepSize = 0.00000001
    val model = LinearRegressionWithSGD.train(parsedData, numIterations, stepSize)

    // Evaluate model on training examples and compute training error
    val valuesAndPreds = parsedData.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }
    val MSE = valuesAndPreds.map{ case(v, p) => math.pow((v - p), 2) }.mean()
    println("training Mean Squared Error = " + MSE)
    
    //Save model
    val path: Boolean=Files.exists(Paths.get("data/LinearRegressionMllib"))
    if (path==false) {
	 model.save(sc, "data/LinearRegressionMllib")
    }
    
    //Building Akka Stream Kafka 
    val config = ConfigFactory.load()    
    
    implicit val system = ActorSystem.create("akkastreamkafka", config)
    implicit val mat = ActorMaterializer()
    var sameModel1 = LinearRegressionModel.load(sc, "data/LinearRegressionMllib")
   
    //Connect to Kafka Server
    val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
      .withBootstrapServers("localhost:9092")
      .withGroupId("AkkaSparkCassandra")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    
    //Work with input of format "1.0 2.0" (two Ints or two doubles)
    //Use the model to predict values based on input format
    //Write the predicted value to Cassandra DB
    Consumer.committableSource(consumerSettings, Subscriptions.topics("KafkaInput"))
      .map(msg => {
	var x1=msg.record.value.toString
	var result=x1.split(" ")	
	var x2:String=result(0)
	var x3:String="B".toString

	if (result.length>1) {
		x2=result(0)
		x3=result(1)	
	} else{
		x2=result(0)
		x3="0"
	}
	
	var ss1:Int=999	
	
	var i1d=x2.toDouble			
	var i2d=x3.toDouble
	if (i1d>0){	
		var ss=sameModel1.predict(Vectors.dense(i1d, i2d))		
		ss1=ss.toInt	
	}

	var x:String="cat"
	x=x+x2.toString	
		
	var collection = sc.parallelize(Seq((x, ss1)))
	collection.saveToCassandra("Spark_Cassandra", "words", SomeColumns("word", "count"))

	
      })
      .runWith(Sink.ignore)	

    // prevent WakeupException on quick restarts of vm
    scala.sys.addShutdownHook {      
      system.terminate()
      Await.result(system.whenTerminated, 30 seconds)
      println(Instant.now)
    }
  }

}
