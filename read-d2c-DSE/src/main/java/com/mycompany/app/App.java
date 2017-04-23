package com.mycompany.app;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.servicebus.ServiceBusException;
import com.mycompany.dao.WindDao;
import com.mycompany.model.Windspeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.function.Consumer;


public class App
{
    private static String connStr = "Endpoint=sb://iothub-ns-jphhonxxxxxxxxxxxxx.servicebus.windows.net/;" +
            "EntityPath=jphhXXXXXX;SharedAccessKeyName=iothubowner;SharedAccessKey=xxxxxxxxxxxxxxxxxxxx=";
    private static WindDao dao;
    private static Logger logger = LoggerFactory.getLogger(App.class);
    public static void main( String[] args ) throws IOException
    {
        String contactPointsStr = System.getProperty("contactPoints", "127.0.0.1");
        dao = new WindDao(contactPointsStr.split(","));
	EventHubClient client0 = receiveMessages("0");
	EventHubClient client1 = receiveMessages("1");
	System.out.println("Press ENTER to exit.");
	System.in.read();
	try
	{
	  client0.closeSync();
	  client1.closeSync();
	  System.exit(0);
	}
	catch (ServiceBusException sbe)
	{
	  System.exit(1);
 	}
    }
    private static Windspeed createWindspeedRecord (String enqueueTime, String DeviceID, String measurement)
    {
        System.out.println(String.format("in createWindspeeed device %s, windspeed %s",DeviceID,measurement));
        Windspeed windspeed = new Windspeed();
        windspeed.setDeviceID(DeviceID);
        windspeed.setEnqueueTime(enqueueTime);
        return windspeed;
    }
 private static EventHubClient receiveMessages(final String partitionId)
 {
   EventHubClient client = null;
   try {
     client = EventHubClient.createFromConnectionStringSync(connStr);
   }
   catch(Exception e) {
     System.out.println("Failed to create client: " + e.getMessage());
     System.exit(1);
   }
   try {
     client.createReceiver( 
       EventHubClient.DEFAULT_CONSUMER_GROUP_NAME,  
       partitionId,  
       Instant.now()).thenAccept(new Consumer<PartitionReceiver>()
     {
       public void accept(PartitionReceiver receiver)
       {
         System.out.println("** Created receiver on partition " + partitionId);
         try {
           while (true) {
             Iterable<EventData> receivedEvents = receiver.receive(100).get();
             int batchSize = 0;
             if (receivedEvents != null)
             {
               for(EventData receivedEvent: receivedEvents)
               {
                   String enqueueTime = receivedEvent.getSystemProperties().getEnqueuedTime().toString();
                   System.out.println(String.format("after enque time %s",enqueueTime));

                 System.out.println(String.format("Offset: %s, SeqNo: %s, EnqueueTime: %s",
                   receivedEvent.getSystemProperties().getOffset(), 
                   receivedEvent.getSystemProperties().getSequenceNumber(), 
                   enqueueTime));
                   String DeviceID = receivedEvent.getSystemProperties().get("iothub-connection-device-id").toString();
                   String measurement = new String(receivedEvent.getBody(), Charset.defaultCharset());
                 System.out.println(String.format("| Device ID: %s", DeviceID));
                 System.out.println(String.format("| Message Payload: %s", measurement));
                  Windspeed windspeed = createWindspeedRecord ( enqueueTime,  DeviceID, measurement);
                   System.out.println(String.format("After created windspeed record"));
                 batchSize++;
                 dao.insertWindspeedAsync(windspeed);
               }
             }
             System.out.println(String.format("Partition: %s, ReceivedBatch Size: %s", partitionId,batchSize));
           }
         }
         catch (Exception e)
         {
           System.out.println("Failed to receive messages: " + e.getMessage());
         }
       }
     });
   }
   catch (Exception e)
   {
     System.out.println("Failed to create receiver: " + e.getMessage());
   }
   return client;
 }


}
