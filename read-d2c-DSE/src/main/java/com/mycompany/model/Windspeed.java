package com.mycompany.model;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


@Table(keyspace = "iothub", name = "windspeed")
public class Windspeed {

	@PartitionKey
	@Column(name = "deviceID")
	private String deviceID;

	@PartitionKey
	@Column(name = "transaction_day")
	private int transaction_day;

	@ClusteringColumn
	@Column(name = "transaction_time")
	private Date transaction_time;

	@Column(name = "windSpeed")
	private Double windSpeed;

	//  2017-04-19T10:18:35-05:00
	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");


	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public Date getEnqueueTime() {
		return transaction_time;
	}

	public void setEnqueueTime(String enqueueTime) {
		String stringTime = enqueueTime.replace('T', ' ');
		String enqueueDay = stringTime.substring(0, 4) + stringTime.substring(5,7) + stringTime.substring (8,10);
		setTransactionDay(enqueueDay);
		Date enqueueDate = null;
		try {
			enqueueDate = formatter.parse(stringTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		this.transaction_time = enqueueDate;
		System.out.println(String.format("in createWindspeeed day %s",enqueueDay));
	}

	public int getTransactionDay() {
		return transaction_day;
	}

	public void setTransactionDay(String transaction_day) {
		this.transaction_day = Integer.parseInt(transaction_day);
	}

	public Double getWindspeed() {
		return windSpeed;
	}

	public void setWindspeed(String windSpeed) {
		this.windSpeed = Double.parseDouble(windSpeed);
	}
}
