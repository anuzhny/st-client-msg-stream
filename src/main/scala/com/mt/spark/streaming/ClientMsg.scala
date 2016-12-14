package com.mt.spark.streaming

/**
  * Created by anuzhny on 02.12.16.
  */

import java.io.ByteArrayOutputStream
import java.util.HashMap

import org.apache.avro.SchemaBuilder
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumWriter
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.serializer.KryoSerializer
import kafka.serializer._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import java.io.{ByteArrayOutputStream, File, IOException}

import org.apache.spark.streaming.{Seconds, StreamingContext, Time}
import org.apache.avro.file.{DataFileReader, DataFileWriter}
import org.apache.avro.generic.{GenericDatumReader, GenericDatumWriter, GenericRecord, GenericRecordBuilder}
import org.apache.avro.io.EncoderFactory
import org.apache.avro.io._
import org.apache.avro.SchemaBuilder
import org.apache.avro.Schema
import org.apache.avro._
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.kafka._
import org.apache.spark.storage.StorageLevel
import org.apache.spark.rdd.RDD
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.DeserializationFeature
import com.github.nscala_time.time.Imports._
//import com.mt.spark.streaming.ClientMsg.ClientMsgStr
import org.joda.time.DateTime
//import org.joda.time._



case class ClientMsgStr(
                         Type: String,
                         MAC: String,
                         NASIP: String,
                         SSID: String,
                         SegmentId: Int,
                         Premium: Boolean,
                         APMAC: String,
                         ClientIP: String,
                         MSISDN: String,
                         Email: String,
                         Address: String,
                         Gender: String,
                         Age: Int,
                         Message: String,
                         Timestamp: Long
                         )

/*
object ClientMsgStr {
  def apply(
             Type: String,
             MAC: String,
             NASIP: String,
             SSID: String,
             SegmentId: Int,
             Premium: Boolean,
             APMAC: String,
             ClientIP: String,
             MSISDN: String,
             Email: String,
             Address: String,
             Gender: String,
             Age: Int,
             Message: String,
             Timestamp: Long): ClientMsgStr = ClientMsgStr(Type,  MAC,  NASIP, SSID, SegmentId, Premium,  APMAC, ClientIP,  MSISDN, Email, Address, Gender, Age, Message, Timestamp, (Timestamp*1000).toDateTime.getYear(),(Timestamp*1000).toDateTime.getMonthOfYear(), (Timestamp*1000).toDateTime.getDayOfMonth())
}

*/

object ClientMsg {
  val msgSchema = SchemaBuilder
    .record("ClientEventRecord_v1")
    .fields
    .name("Type").`type`().stringType().stringDefault("")
    .name("MAC").`type`().stringType().stringDefault("")
    .name("NASIP").`type`().stringType().stringDefault("")
    .name("SSID").`type`().stringType().stringDefault("")
    .name("SegmentId").`type`().intType().intDefault(0)
    .name("Premium").`type`().booleanType().booleanDefault(false)
    .name("APMAC").`type`().stringType().stringDefault("")
    .name("ClientIP").`type`().stringType().stringDefault("")
    .name("MSISDN").`type`().stringType().stringDefault("")
    .name("Email").`type`().stringType().stringDefault("")
    .name("Address").`type`().stringType().stringDefault("")
    .name("Gender").`type`().stringType().stringDefault("")
    .name("Age").`type`().intType().intDefault(0)
    .name("Message").`type`().stringType().stringDefault("")
    .name("Timestamp").`type`().longType().longDefault(0)
    .endRecord

  def main(args: Array[String]) {
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
    Logger.getLogger("org.apache.spark.storage.BlockManager").setLevel(Level.ERROR)

    val logger: Logger = Logger.getLogger("com.sparkdeveloper.receiver.KafkaConsumer")
    val sparkConf = new SparkConf().setAppName("ClientMsg Collector")

    sparkConf.set("spark.cores.max", "6")
    sparkConf.set("spark.serializer", classOf[KryoSerializer].getName)
    sparkConf.set("spark.kryoserializer.buffer.max.mb", "1024")
    sparkConf.set("spark.sql.tungsten.enabled", "true")
    sparkConf.set("spark.eventLog.enabled", "true")
    sparkConf.set("spark.app.id", "ClientMsg Collector") // want to know your app in the UI
    sparkConf.set("spark.io.compression.codec", "snappy")
    sparkConf.set("spark.rdd.compress", "true")
    sparkConf.set("spark.streaming.backpressure.enabled", "true")

    sparkConf.set("spark.sql.parquet.compression.codec", "snappy")
    sparkConf.set("spark.sql.parquet.mergeSchema", "true")
    sparkConf.set("spark.sql.parquet.binaryAsString", "true")

    val sc = new SparkContext(sparkConf)
    sc.hadoopConfiguration.set("parquet.enable.summary-metadata", "false")
    val ssc = new StreamingContext(sc, Seconds(60*30))

    try {
      val kafkaConf = Map(
        "zookeeper.connect" -> "stmgn001.vmet.ro,stname001.vmet.ro,stjobt001.vmet.ro",
        "group.id" -> "group_spark",
        "zookeeper.connection.timeout.ms" -> "1000")

      val topicMaps = Map("client_eve_spark" -> 1)

      // Create a new stream which can decode byte arrays.
      val clientMsg = KafkaUtils.createStream[String, Array[Byte], DefaultDecoder, DefaultDecoder](ssc, kafkaConf,topicMaps, StorageLevel.MEMORY_ONLY_SER)

      try {
        clientMsg.foreachRDD((rdd, time) => {
          if (rdd != null) {
            try {
              val sqlContext = new SQLContext(sc)
              import sqlContext.implicits._

              val rdd2 = rdd.map { case (k, v) => parseAVROToString(v) }

              try {
                val result = rdd2.mapPartitions(records => {
                  val mapper = new ObjectMapper()
                  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                  mapper.registerModule(DefaultScalaModule)
                  records.flatMap(record => {
                    try {
                      Some(mapper.readValue(record, classOf[ClientMsgStr]))
                    } catch {
                      case e: Exception => None;
                    }
                  })
                }, true)

                def savePart(group : Tuple3[Int,Int,Int], data: Iterable[ClientMsgStr]) {
                  val saveRootPath: String = "/user/stcuscol/result/clients_events/year=" + group._1.toString + "/month=" + group._2.toString + "/day=" + group._3.toString +"/"
                  val df1 = data.toList.toDF()
                  df1.save(saveRootPath, "parquet", org.apache.spark.sql.SaveMode.valueOf("Append"))
                }

                def groupName(Ts: Long) = {

                  ((Ts*1000).toDateTime.withZone(DateTimeZone.forID("Etc/GMT-3")).getYear,(Ts*1000).toDateTime.withZone(DateTimeZone.forID("Etc/GMT-3")).getMonthOfYear, (Ts*1000).toDateTime.withZone(DateTimeZone.forID("Etc/GMT-3")).getDayOfMonth)
                }

                val res = result.groupBy(r => groupName(r.Timestamp)).collect()
                res.foreach(msg => savePart(msg._1, msg._2))

              } catch {
                case e: Exception =>
                  println("Map value. Exception:" + e.getMessage);
                  e.printStackTrace();
              }
            }
            catch {
              case e: Exception =>
                println("Create RDD. Exception:" + e.getMessage);
                e.printStackTrace();
            }
          }
        })
      } catch {
        case e: Exception =>
          println("Writing files after job. Exception:" + e.getMessage);
          e.printStackTrace();
      }
    } catch {
      case e: Exception =>
        println("Kafka Stream. Writing files after job. Exception:" + e.getMessage);
        e.printStackTrace();
    }
    ssc.start()
    ssc.awaitTermination()
  }

  def parseAVROToString(rawClientMsg: Array[Byte]): String = {
    try {
      if (rawClientMsg.isEmpty) {
        println("Rejected ClientMsg")
        "Empty"
      }
      else {
        deserializeClientMsg(rawClientMsg).toString
      }
    } catch {
      case e: Exception =>
        println("Exception:" + e.getMessage)
        "Empty"
    }
  }

  //Deserialize client message using avro scheme
  def deserializeClientMsg(msg: Array[Byte]): GenericRecord = {
    try {
      val reader = new GenericDatumReader[GenericRecord](msgSchema)
      val decoder = DecoderFactory.get().binaryDecoder(msg, null)
      reader.read(null, decoder)
    } catch {
      case e: Exception =>
        null;
    }
  }
}




