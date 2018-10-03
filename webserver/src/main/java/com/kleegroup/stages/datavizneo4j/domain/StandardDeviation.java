package com.kleegroup.stages.datavizneo4j.domain;


public final class StandardDeviation {
	private String insee;
	private Double[] moyenne_road;
	private Double[] moyenne_vald;
	private Double[]  road;
	private Double[]  vald;
	public String getInsee() {
		return insee;
	}
	public Double[] getMoyenne_road() {
		return moyenne_road;
	}
	public Double[] getMoyenne_vald() {
		return moyenne_vald;
	}
	public Double[] getRoad() {
		return road;
	}
	public Double[] getVald() {
		return vald;
	}
	public StandardDeviation(String insee) {
		this.insee = insee;
		this.moyenne_road=new Double[5];
		this.moyenne_vald=new Double[5];
		this.road = new Double[5];
		this.vald = new Double[5];	
	}
	
}
