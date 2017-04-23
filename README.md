#Read-D2C-DSE
git@github.com:jphaugla/azureIOTDSE.git
========================
See here for use case and requirements 

A company wants to use Microsoft Azure IOT Hub to manage devices collecting windspeed measurements in real-time and write that data to a DataStax database.

In order to run this demo, you need an IOT hub created.

##  Azure IOT Hub
The code here was written using the steps from this Microsoft Azure link:

https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-java-java-getstarted 

Read this full page from the link above before starting to understand the flow.  Here are some notes from me:
This code contains several sections.  
  create-device-identity, creates a device identity and associated security key to connect your simulated device app.
  read-d2c-messages, displays the telemetry sent by your simulated device app.
  simulated-device, connects to your IoT hub with the device identity created earlier, and sends a telemetry message every second using the MQTT protocol.

From these sections, 
	create-device-identiy needs no changes
	simulated-device needs no changes but could not get the java version to work so relied on the node.js version
	read-d2c-messages   instead of just displaying these messages as the azure code does, I write the messages to a DataStax database

The trickiest part to using the IOT hub is understanding which connection information is needed in each case and not making any typos in the various names and keys comprising the keynames.   

# Running demo

Must have maven installed 

##Getting and running the demo

###Download the source from GitHub.

  * Navigate to the directory where you would like to save the code.
  * Execute the following command:
 
 
       `git clone git@github.com:jphaugla/azureIOTDSE.git`

 * Create cql tables
     *  NOTE:  iothub keyspace is created with SimpleStrategy-change this if running on more than one datacenter!

    `cqlsh -f read-d2c-DSE/src/main/resources/cql/create_schema.cql`

###Confiugre for you IOT hub

refer back to 
https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-java-java-getstarted

update connection information in the following files:

./create-device-identity/src/main/java/com/mycompany/app/App.java
./read-d2c-DSE/src/main/java/com/mycompany/app/App.java
./simulateddevice/SimulatedDevice.js


NOTE:  the connection string will be called connectionString or connStr
