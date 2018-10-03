package com.kleegroup.stages.datavizneo4j.domain;

public final class Town {
	private int id;
	private String name;
	private int county_nb;
	private int total_pop;
	private String geoshape;
	private String geopoint;
	private double shape_area;

	public Town(int id, String name, int county_nb, int total_pop, String geoshape, String geopoint,double shape_area) {
		this.id = id;
		this.name = name;
		this.county_nb = county_nb;
		this.total_pop = total_pop;
		this.geoshape = geoshape;
		this.geopoint = geopoint;
		this.shape_area=shape_area;
	}

	public Town(int id, String name, int county_nb, String geoshape, String geopoint) {
		this.id = id;
		this.name = name;
		this.county_nb = county_nb;
		this.geoshape = geoshape;
		this.geopoint = geopoint;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getCounty_nb() {
		return county_nb;
	}

	public int getTotal_pop() {
		return total_pop;
	}

	public String getGeoshape() {
		return geoshape;
	}

	public String getGeopoint() {
		return geopoint;
	}

	public double getShape_area() {
		return shape_area;
	}

}
