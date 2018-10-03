package com.kleegroup.stages.datavizneo4j.domain;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.HashCodeBuilder;

public final class RouteSection {

	private final String stop1_id;
	private final String stop2_id;
	private final String route_id;

	public RouteSection(final String route_id, final String stop1_id, final String stop2_id) {
		this.route_id = route_id;
		this.stop1_id = stop1_id;
		this.stop2_id = stop2_id;
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

	public boolean follows(final RouteSection other) {
		return route_id.equals(other.route_id) && stop1_id.equals(other.stop2_id);
	}

	public RouteSection reversed() {
		return new RouteSection(route_id, stop2_id, stop1_id);
	}

	@Override
	public String toString() {
		return "{route_id: " + route_id + ", stop1_id: " + stop1_id + ", stop2_id: " + stop2_id + "}";
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (!RouteSection.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		final RouteSection other = (RouteSection) obj;
		if ((route_id == null) ? (other.route_id != null) : !route_id.equals(other.route_id)) {
			return false;
		}
		if ((stop1_id == null) ? (other.stop1_id != null) : !stop1_id.equals(other.stop1_id)) {
			return false;
		}
		if ((stop2_id == null) ? (other.stop2_id != null) : !stop2_id.equals(other.stop2_id)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append((route_id.hashCode()))
				.append((stop1_id.hashCode()))
				.append((stop2_id.hashCode()))
				.toHashCode();
	}

}
