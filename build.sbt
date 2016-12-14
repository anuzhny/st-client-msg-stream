name := "st-client-msg-stream"

version := "1.0"

scalaVersion := "2.10.4"

version := "2.0"

organization := "com.mt"

scalacOptions ++= Seq("-unchecked", "-deprecation")

assemblyJarName in assembly := "st-client-msg-stream-assembly_2.10-1.0.jar"

resolvers += "OSS Sonatype" at "https://repo1.maven.org/maven2/"

resolvers += "Cloudera Repository" at "https://repository.cloudera.com/artifactory/cloudera-repos/"

libraryDependencies += "org.apache.spark" %  "spark-assembly_2.10"   % "1.3.0-cdh5.4.2" % "provided"

libraryDependencies += "org.apache.spark" % "spark-core_2.10" % "1.3.0-cdh5.4.2" % "provided"

libraryDependencies += "org.apache.kafka" %  "kafka-clients" % "0.8.2.1" % "provided"

libraryDependencies += "org.apache.kafka" % "kafka_2.10" % "0.8.2.1" % "provided"

libraryDependencies += "org.apache.spark" % "spark-streaming-kafka_2.10" % "1.3.0-cdh5.4.2"

libraryDependencies += "org.apache.avro" % "avro" % "1.7.6" %  "provided"

libraryDependencies  += "org.codehaus.jackson" % "jackson-mapper-asl" % "1.9.9" % "provided"

libraryDependencies += "com.fasterxml.jackson.module" % "jackson-module-scala_2.10" % "2.2.2"

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.2.2" % "provided"

libraryDependencies  += "com.google.code.gson" % "gson" % "2.2.4" % "provided"

libraryDependencies  += "com.github.nscala-time" %% "nscala-time" % "1.8.0"

mergeStrategy in assembly := {
  case m if m.toLowerCase.endsWith("manifest.mf")          => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$")      => MergeStrategy.discard
  case "log4j.properties"                                  => MergeStrategy.discard
  case m if m.toLowerCase.startsWith("meta-inf/services/") => MergeStrategy.filterDistinctLines
  case "reference.conf"                                    => MergeStrategy.concat
  case _                                                   => MergeStrategy.first
}