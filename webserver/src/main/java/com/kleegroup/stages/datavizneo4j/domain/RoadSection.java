package com.kleegroup.stages.datavizneo4j.domain;

public final class RoadSection {

	private int id;
	private int id_arc_tra;
	private String geoshape;
	private String geopoint;
	private HourSummary hour;

	public RoadSection(int id, int id_arc_tra, String geoshape, String geopoint) {
		this.id = id;
		this.id_arc_tra = id_arc_tra;
		this.geoshape = geoshape;
		this.geopoint = geopoint;
	}

	public RoadSection(int id, int id_arc_tra, String geoshape, String geopoint, HourSummary hour) {
		this.id = id;
		this.id_arc_tra = id_arc_tra;
		this.geoshape = geoshape;
		this.geopoint = geopoint;
		this.hour = hour;
	}

	public int getId() {
		return id;
	}

	public int getId_arc_tra() {
		return id_arc_tra;
	}

	public String getGeoshape() {
		return geoshape;
	}

	public String getGeopoint() {
		return geopoint;
	}

	public HourSummary getHour() {
		return hour;
	}
}
