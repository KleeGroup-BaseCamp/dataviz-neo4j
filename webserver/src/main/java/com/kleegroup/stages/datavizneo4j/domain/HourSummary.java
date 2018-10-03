package com.kleegroup.stages.datavizneo4j.domain;

public final class HourSummary {

	private double speed;
	private double rateFlow;
	private double rate;
	private String timeSlot;
	private int nb_val;

	public int getNb_val() {
		return nb_val;
	}

	public double getSpeed() {
		return speed;
	}

	public double getRateFlow() {
		return rateFlow;
	}

	public double getRate() {
		return rate;
	}

	public String getTimeSlot() {
		return timeSlot;
	}

	//For roads
	public HourSummary(double speed, double rateFlow, double rate, String timeSlot) {
		super();
		this.speed = speed;
		this.rateFlow = rateFlow;
		this.rate = rate;
		this.timeSlot = timeSlot;
	}

	//For stops
	public HourSummary(int nb_val, String timeSlot) {
		this.nb_val = nb_val;
		this.timeSlot = timeSlot;
	}

}
