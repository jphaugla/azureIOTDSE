package com.mycompany.dao;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.ConstantSpeculativeExecutionPolicy;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.mycompany.demo.utils.AsyncWriterWrapper;
import com.mycompany.model.Windspeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class WindDao
{

    private static final String keyspaceName = "iothub";
    private static final String windspeedTable = keyspaceName + ".windspeed";
    private static final String INSERT_INTO_WINDSPEED = "insert into " + windspeedTable +
            " (deviceID, transaction_time, transaction_day, windSpeed) values (?,?,?,?);";

    private PreparedStatement insertWindspeed;
    private static Logger logger = LoggerFactory.getLogger(WindDao.class);
    private Session session;


    public WindDao(String[] contactPoints) {

        ConstantSpeculativeExecutionPolicy policy =
                new ConstantSpeculativeExecutionPolicy(5,3);
        Cluster cluster = Cluster.builder()
                .addContactPoints(contactPoints)
                .withSpeculativeExecutionPolicy(policy)
                .withLoadBalancingPolicy(new TokenAwarePolicy(DCAwareRoundRobinPolicy.builder().build()))
                .build();


        this.session = cluster.connect();

        this.insertWindspeed = session.prepare(INSERT_INTO_WINDSPEED);
        logger.debug("Creating new DAO in thread: " + Thread.currentThread());
    }

    public void saveWindspeed(Windspeed windspeed) {
        this.insertWindspeedAsync(windspeed);
    }

    public void insertWindspeedsAsync(List<Windspeed> Windspeeds) {

        AsyncWriterWrapper wrapper = new AsyncWriterWrapper();
        for (Windspeed Windspeed : Windspeeds) {
            wrapper.addStatement(insertWindspeed.bind(Windspeed.getDeviceID(), Windspeed.getEnqueueTime(), Windspeed.getTransactionDay(),
                    Windspeed.getWindspeed()));
        }
        wrapper.executeAsync(this.session);
    }

    public void insertWindspeedAsync(Windspeed Windspeed) {

        AsyncWriterWrapper wrapper = new AsyncWriterWrapper();

        wrapper.addStatement(insertWindspeed.bind(Windspeed.getDeviceID(), Windspeed.getEnqueueTime(), Windspeed.getTransactionDay(),
                Windspeed.getWindspeed()));
        wrapper.executeAsync(this.session);
    }


}
