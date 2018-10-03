package com.kleegroup.stages.datavizneo4j.domain;

import org.apache.commons.lang.builder.HashCodeBuilder;

public final class StopNode {

	private final String name;
	private final double lat;
	private final double lon;
	private final int nb_vald;

	public StopNode(final String name, final double lat, final double lon, final int nb_vald) {
		this.lat = lat;
		this.lon = lon;
		this.name = name;
		this.nb_vald = nb_vald;
	}

	public String getName() {
		return name;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	public int getNb_vald() {
		return nb_vald;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (!StopNode.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		final StopNode other = (StopNode) obj;
		if ((name == null) ? (other.name != null) : !name.equals(other.name)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append((int) (lat - 48) * 1000).append((int) (lon - 2) * 1000).toHashCode();
	}

}
