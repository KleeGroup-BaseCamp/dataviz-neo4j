package com.kleegroup.stages.datavizneo4j.domain;

public final class LimitationArea {
	private int id;
	private String geoshape;
private int speed_limitation ;
	public LimitationArea(int _id,  String _geoshape, int _limitation_speed) {
		super();
		this.geoshape = _geoshape;
		this.speed_limitation = _limitation_speed;
		this.id = _id ; 
	}

	public int getId() {
		return id;
	}
	
	public String getGeoshape() {
		return geoshape;
	}
	
	public int getSpeedLimitation() {
		return speed_limitation;
	}



}
