package com.datastax.spark.eventhub

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.nio.charset.Charset

import com.datastax.spark.connector._
import com.google.gson.GsonBuilder
import com.microsoft.azure.eventhubs.EventData
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.eventhubs.EventHubsUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

//  Windspeed matches structure of DataStax Cassandra table to allow saveToCassandra
case class Windspeed (deviceid: String, transaction_day: Int, transaction_time: Timestamp, windspeed: Double)
//  JSON Message coming from eventhub has this structure
case class msgBody (deviceID: String, windSpeed: Double)


/**
 * Read from EventHub and write to DataStax cassandra
 */
object EventhubDataStaxStream {



  def main(args: Array[String]): Unit = {

    if (args.length != 6) {
      println("Usage: program progressDir PolicyName PolicyKey EventHubNamespace EventHubName" +
        " BatchDuration(seconds)")
      sys.exit(1)
    }

    val progressDir = args(0)
    val policyName = args(1)
    val policykey = args(2)
    val eventHubNamespace = args(3)
    val eventHubName = args(4)
    val batchDuration = args(5).toInt

    val eventhubParameters = Map[String, String](
      "eventhubs.policyname" -> policyName,
      "eventhubs.policykey" -> policykey,
      "eventhubs.namespace" -> eventHubNamespace,
      "eventhubs.name" -> eventHubName,
      "eventhubs.partition.count" -> "4",
      "eventhubs.consumergroup" -> "$Default"
    )

    val ssc = new StreamingContext(new SparkContext(), Seconds(batchDuration))
    System.out.println(s"after ssc creation with progressDir $progressDir policy name $policyName policykey " +
      s"$policykey namespace $eventHubNamespace name $eventHubName batch $batchDuration")
    val inputDirectStream = EventHubsUtils.createDirectStreams(
      ssc,
      eventHubNamespace,
      progressDir,
      Map(eventHubName -> eventhubParameters))
    System.out.println(s"before for loop")
    inputDirectStream.foreachRDD {
      (message: RDD[(EventData)]) => {
         val df = message.map(eventdata => {
          // 2017-05-01T16:28:22.172Z  so must remove the "T" and not use trailing "Z"
          // yyyy-mm-dd hh:mm:ss.rrr
          val enqueueTimeString = eventdata.getSystemProperties().getEnqueuedTime().toString()
          val enqueueDayDash = enqueueTimeString.substring(0,10).concat(" ").concat(enqueueTimeString.substring(11,23))
          val ts = Timestamp.valueOf(enqueueDayDash)
          val dayFormat = new SimpleDateFormat("YYYYMMdd")

          val DeviceID = eventdata.getSystemProperties.get("iothub-connection-device-id").toString

          val enqueueDay = dayFormat.format(ts).toInt

          //  pull mesgBody out of the eventdata

           val msgBody = new String(eventdata.getBody, Charset.defaultCharset)

           //  convert json message body to the msgBody record
           val gson = new GsonBuilder().create()
           val dataclass = classOf[msgBody]
           val thisRec:msgBody = gson.fromJson(msgBody, dataclass)
           //   use case class to format message correctly for writing to cassandra
          Windspeed(DeviceID, enqueueDay, ts, thisRec.windSpeed)
        }
        ).saveToCassandra("iothub", "windspeed",
          SomeColumns("deviceid", "transaction_day", "transaction_time", "windspeed"))

      }
    }
        ssc.start()
        ssc.awaitTermination()
    }
  }

