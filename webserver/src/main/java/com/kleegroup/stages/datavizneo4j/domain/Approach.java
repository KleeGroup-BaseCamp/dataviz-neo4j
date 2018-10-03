package com.kleegroup.stages.datavizneo4j.domain;

public class Approach {
	private String vehicleRef;
	private String aimedArrivalTime;
	private String aimedDepartureTime;
	private String arrivalStatus;
	private String destinationRef;
	private String expectedArrivalTime;
	private String expectedDepartureTime;
	
	public String getExpectedDepartureTime() {
		return expectedDepartureTime;
	}
	public String getVehicleRef() {
		return vehicleRef;
	}
	public String getAimedArrivalTime() {
		return aimedArrivalTime;
	}
	public String getAimedDepartureTime() {
		return aimedDepartureTime;
	}
	public String getArrivalStatus() {
		return arrivalStatus;
	}
	public String getDestinationRef() {
		return destinationRef;
	}
	public String getExpectedArrivalTime() {
		return expectedArrivalTime;
	}
	
	public Approach(String vehicleRef, String aimedArrivalTime, String aimedDepartureTime, String arrivalStatus,
			String destinationRef, String expectedArrivalTime, String expectedDepartureTime) {
		this.vehicleRef = vehicleRef;
		this.aimedArrivalTime = aimedArrivalTime;
		this.aimedDepartureTime = aimedDepartureTime;
		this.arrivalStatus = arrivalStatus;
		this.destinationRef = destinationRef;
		this.expectedArrivalTime = expectedArrivalTime;
		this.expectedDepartureTime=expectedDepartureTime;
	}
	

}
