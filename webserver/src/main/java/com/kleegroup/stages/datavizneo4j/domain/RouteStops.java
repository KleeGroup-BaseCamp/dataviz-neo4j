package com.kleegroup.stages.datavizneo4j.domain;

import java.util.List;

public final class RouteStops {

	private final String route_name;
	private final List<String> beginning_stops;
	private final List<String> end_stops;

	public RouteStops(final String route_name, final List<String> beginning_stops, final List<String> end_stops) {
		this.route_name = route_name;
		this.beginning_stops = beginning_stops;
		this.end_stops = end_stops;
	}

	public String getRouteName() {
		return route_name;
	}

	public List<String> getStopsBeginning() {
		return beginning_stops;
	}

	public List<String> getStopsEnd() {
		return end_stops;
	}

	public void addBeginningStop(final String stopname) {
		this.beginning_stops.add(stopname);
	}

	public void addEndStop(final String stopname) {
		this.end_stops.add(stopname);
	}

}
