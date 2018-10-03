package com.kleegroup.stages.datavizneo4j.domain;

public final class NodeFromRouteSection {

	private final String stop_name;
	private final String routeSection_id;
	private int nb_vald;

	public NodeFromRouteSection(final String stop_name, final String routeSection_id, final int nb_vald) {
		this.stop_name = stop_name;
		this.routeSection_id = routeSection_id;
		this.nb_vald = nb_vald;

	}

	public String getStop_name() {
		return stop_name;
	}

	public String getRouteSection_id() {
		return routeSection_id;
	}

	public int getNb_vald() {
		return nb_vald;
	}

	public void setNb_vald(final int vald) {
		this.nb_vald = vald;
	}

	@Override
	public String toString() {
		return "{name : " + this.stop_name + ", nb_vald :  " + this.nb_vald + "}";
	}

}
