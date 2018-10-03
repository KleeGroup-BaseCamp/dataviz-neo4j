package com.kleegroup.stages.datavizneo4j.domain;

public final class Route {

	private String id;
	private int type;
	private String short_name;
	private String long_name;
	private String geoshape;

	public String getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public String getShort_name() {
		return short_name;
	}

	public String getLong_name() {
		return long_name;
	}

	public String getGeoshape() {
		return geoshape;
	}

	public Route(String id, int type, String short_name, String long_name, String geoshape) {
		this.id = id;
		this.type = type;
		this.short_name = short_name;
		this.long_name = long_name;
		this.geoshape = geoshape;
	}

}
