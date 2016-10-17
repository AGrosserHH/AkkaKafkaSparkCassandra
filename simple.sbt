name := "AkkaKafkaSparkCassandra"

version := "1.0"

scalaVersion := "2.11.8"

assemblyMergeStrategy in assembly := {
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
 case n if n.startsWith("reference.conf") => MergeStrategy.concat
 case x => MergeStrategy.first
}

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.0.0-preview"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.0.0-preview"

libraryDependencies += "org.apache.spark" % "spark-mllib_2.11" % "2.0.0-preview"

libraryDependencies += "org.apache.spark" %% "spark-streaming" % "2.0.0-preview"

libraryDependencies += "org.apache.spark" % "spark-streaming-kafka-0-8_2.11" % "2.0.0-preview"

resolvers += "Spark Packages Repo" at "https://dl.bintray.com/spark-packages/maven"

libraryDependencies += "datastax" % "spark-cassandra-connector" % "2.0.0-M2-s_2.11"

libraryDependencies +="com.datastax.spark"  %% "spark-cassandra-connector-embedded" % "2.0.0-M2"

libraryDependencies += "com.typesafe.akka" %% "akka-stream-kafka" % "0.12"


