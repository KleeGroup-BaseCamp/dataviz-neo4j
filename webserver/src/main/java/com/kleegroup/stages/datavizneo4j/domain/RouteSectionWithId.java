package com.kleegroup.stages.datavizneo4j.domain;

import java.util.ArrayList;
import java.util.List;

public final class RouteSectionWithId {

	private final String id;
	private final String stop1_id;
	private final String stop2_id;
	private final String route_id;

	public RouteSectionWithId(final String id, final String route_id, final String stop1_id, final String stop2_id) {
		this.id = id;
		this.route_id = route_id;
		this.stop1_id = stop1_id;
		this.stop2_id = stop2_id;

	}

	public String getId() {
		return id;
	}

	public String getRoute_id() {
		return route_id;
	}

	public String getStop1_id() {
		return stop1_id;
	}

	public String getStop2_id() {
		return stop2_id;
	}

	public List<String> toList() {
		final List<String> list = new ArrayList<>();
		list.add(route_id);
		list.add(stop1_id);
		list.add(stop2_id);
		return list;

	}

}
