package com.kleegroup.stages.datavizneo4j.domain;

import java.util.List;

public final class RouteTraficInfo {

	private final String name;
	private final int nb_vald; // par jour
	private final float nb_disturbances; // moyen, par jour
	private final List<StopTraficInfo> stops_info; // La liste des informations sur chacun des stops
	private final List<RouteSection> routeSections; // La liste des routeSections composant la route

	public RouteTraficInfo(final String name, final int nb_vald, final float nb_disturbances,
			final List<StopTraficInfo> stops_info, final List<RouteSection> routeSections) {
		this.name = name;
		this.nb_vald = nb_vald; // par jour
		this.nb_disturbances = nb_disturbances; // par mois
		this.stops_info = stops_info;
		this.routeSections = routeSections;

	}

	public String getName() {
		return name;
	}

	public int getNb_vald() {
		return nb_vald;
	}

	public List<StopTraficInfo> getStops_info() {
		return stops_info;
	}

	public List<RouteSection> getRouteSections() {
		return routeSections;
	}

}
