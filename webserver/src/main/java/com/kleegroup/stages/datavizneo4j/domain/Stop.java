package com.kleegroup.stages.datavizneo4j.domain;

public final class Stop {

	private double lat;
	private double lon;
	private String id;
	private String name;
	private int location_type;
	private HourSummary hour;
	private int ontime;
	private int delays;
	private int cancel;
	private Approach approach;

	public Stop(double lat, double lon, String id, String name, int location_type, HourSummary hour) {
		this.lat = lat;
		this.lon = lon;
		this.id = id;
		this.name = name;
		this.location_type = location_type;
		this.hour = hour;
	}
	public Stop(double lat, double lon, String id, String name, int location_type) {
		this.lat = lat;
		this.lon = lon;
		this.id = id;
		this.name = name;
		this.location_type = location_type;
	}

	public Stop(double lat, double lon, String id, String name, int location_type, int ontime, int delays, int cancel) {
		this.lat = lat;
		this.lon = lon;
		this.id = id;
		this.name = name;
		this.location_type = location_type;
		this.ontime = ontime;
		this.delays = delays;
		this.cancel = cancel;
	}
	
	public Stop(double lat, double lon, String id, String name, int location_type,Approach approach) {
		this.lat = lat;
		this.lon = lon;
		this.id = id;
		this.name = name;
		this.location_type = location_type;
		this.approach=approach;
	}
	
	public HourSummary getHour() {
		return hour;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}
	public int getOntime() {
		return ontime;
	}
	public int getDelays() {
		return delays;
	}
	public int getCancel() {
		return cancel;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getLocation_type() {
		return location_type;
	}
	public Approach getApproach() {
		return approach;
	}

}
