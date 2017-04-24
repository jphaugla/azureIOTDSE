create keyspace if not exists iothub WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1' };

use iothub;


create table if not exists windspeed(
        deviceID text,
        transaction_time timestamp,
        transaction_day int,
        windSpeed double,
        PRIMARY KEY ((deviceID,transaction_day), transaction_time)
) WITH CLUSTERING ORDER BY (transaction_time desc);
