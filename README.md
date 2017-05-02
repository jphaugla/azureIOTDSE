#azureIOTHub
========================
See here for use case and requirements 
[git@github.com:jphaugla/azureIOTDSE.git]()

A company wants to use Microsoft Azure IOT Hub to manage devices collecting windspeed measurements in real-time and write that data to a DataStax database.

In order to run this demo, you need an IOT hub created.

This github demonstrates two ways to recieve messages from Azure Eventhub and write to DataStax Cassandra.  The first way uses the datastax java driver to write asynchronously to DataStax.  The second example uses spark streaming with the Cassandra Connector to write to DataStax Cassandra from the Azure EventHub

#Azure IOT Hub using DataStax java drivers asynchronously

The code here was written using the steps from this Microsoft Azure link:

[https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-java-java-getstarted ]()

Read this full page from the link above before starting to understand the flow.  Here are some notes from me:
This code contains several sections:
  
*  create-device-identity, creates a device identity and associated security key to connect your simulated device app.
  
*  read-d2c-messages, displays the telemetry sent by your simulated device app.
 
*  simulateddevice, connects to your IoT hub with the device identity created earlier, and sends a telemetry message every second using the MQTT protocol.

These instructions build the maven repository (think pom.xml).  The steps in the directions must be followed completely to properly build the repository.  So, the overallflow is:

1. Follow instructions in the azure documentation to build each of the repositories.
2. Use git clone to pull the code I have built in similarly named but different directories
3. Copy only the needed changes from my directories over to the new  

From these sections:

*  	create-device-identiy needs no changes
  
*  	simulateddevice needs no changes but could not get the java version to work so relied on the node.js version
   
*  	read-d2c-messages   instead of just displaying these messages as the azure code does, I write the messages to a DataStax database

The trickiest part to using the IOT hub is understanding which connection information is needed in each case and not making any typos in the various names and keys comprising the keynames. 

#Detail Steps
  
#1. Follow instructions in the azure documentation to build each of the repositories.  Make sure to select Java instead of .NET on the left side below the Filter.

[https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-java-java-getstarted ]()

a) Follow the steps to create an IOT hub
b) create-device-entity only need to follow first step to create the repository 
`mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=create-device-identity -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false`
c) Can skip the rest of the steps listed under create-device-entity because a completed pom.xml and App.java is provided when the repository is pulled from the github repository
d) Continue the next section "Receive device-to-cloud messages" running the first step to create the repository 
`mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=read-d2c-messages -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false`
e) Can skip the rest of the steps listed under receive device to cloud because a completed pom.xml and App.java is provided when the repository is pulled from the github repository
f) Continue the next section "Create a simulated device app".  However, in this case, the java code did not work for me so I used the java script code.  On the left side of the web page, select Node.js instead of Java.
g) Run the first two steps documented 

 `npm init`
 `npm install azure-iot-device azure-iot-device-mqtt --save`
 h) Can skip the rest of the steps listed as the repository contains the code for SimulatedDevice.js


#2. Use git clone to pull the code 
Git clone will pull code from the repository into directories with sligthly different directory names to allow copying files back to the maven created directories.

maven must be installed 

##Download the source from GitHub.

  * Navigate to the directory where you would like to save the code.
  * Execute the following command:
 
       `git clone git@github.com:jphaugla/azureIOTDSE.git`
  
  this will create following directories:  create-device-identity-DSE, simulateddevice-DSE, read-d2c-DSE

 * Create cql tables
     *  NOTE:  iothub keyspace is created with SimpleStrategy-change this if running on more than one datacenter!

    `cqlsh -f read-d2c-DSE/src/main/resources/cql/create_schema.cql`

##Configure Code for your IOT hub

Copy following files from the repository directory to the main directory

`cp -p simulateddevice-DSE/SimulatedDevice.js simulateddevice`
`cp -p read-d2c-DSE/pom.xml read-d2c-messages`
`cp -p read-d2c-DSE/src/main/java/com/mycompany/app/App.java read-d2c-messages/src/main/java/com/mycompany/app`
`cp -p create-device-identity-DSE/src/main/java/com/mycompany/app/App.java create-device-identy/src/main/java/com/mycompany/app`


update connection information in the following files:

./create-device-identity/src/main/java/com/mycompany/app/App.java
./read-d2c-messages/src/main/java/com/mycompany/app/App.java
./simulateddevice/SimulatedDevice.js


NOTE:  the connection string will be called connectionString or connStr

## Compile Source Code
 see [https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-java-java-getstarted#create-a-device-identity]()
 
 cd create-device-identity/

mvn clean package -DskipTests


 
cd ../read-d2c-DSE
mvn clean package -DskipTests

##  Run the Code

Use these instructions to run the code:

[https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-java-java-getstarted#run-the-apps]()

### start the read messages program

cd read-d2c-messages

mvn exec:java -Dexec.mainClass="com.mycompany.app.App" 

### start simulated messages

cd simulateddevice

node SimulatedDevice.js

#Streaming version of code using Spark Streaming from Azure Eventhub 

instead of receiving the messages using read-d2c-DSE directory, use streamReadIOT
built off this github https://github.com/hdinsight/spark-eventhubs


cd streamReadIOT

mvn clean package -DskipTests

./runit.sh


start simulated device as before
