package com.kleegroup.stages.datavizneo4j.domain;

import java.util.List;

public final class StopsLink {

	private final String source;
	private final String target;
	private final int total_valds;
	private final String name;

	public StopsLink(final String source, final String target, final int total_valds, final String name) {
		this.source = source;
		this.target = target;
		this.total_valds = total_valds;
		this.name = name;

	}

	public String getSource() {
		return source;
	}

	public String getTarget() {
		return target;
	}

	public int getTotalValds() {
		return total_valds;
	}

	public String getName() {
		return name;
	}

	public boolean hasOppositeLinkInList(final List<StopsLink> links) {

		for (final StopsLink link : links) {
			if (link.getSource().equals(this.getTarget()) && link.getTarget().equals(this.getSource())) {
				return true;
			}
		}

		return false;
	}

	public boolean follows(final StopsLink otherLink) {

		return this.getSource().equals(otherLink.getTarget());
	}

	public boolean precedes(final StopsLink otherLink) {

		return this.getTarget().equals(otherLink.getSource());
	}

	public StopsLink previousLink(final List<StopsLink> links) {

		for (final StopsLink link : links) {
			if (this.name == link.name && this.follows(link)) {
				return link;
			}
		}

		return null; // Pas de noeud précédent

	}

	public StopsLink nextLink(final List<StopsLink> links) {

		for (final StopsLink link : links) {
			if (this.name == link.name && this.precedes(link)) {
				return link;
			}
		}

		return null; // Pas de noeud précédent

	}

	@Override
	public String toString() {
		return this.source + " -> " + this.target;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (!StopsLink.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		final StopsLink other = (StopsLink) obj;
		/*	if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) { / On l'enlève pour most_frequented_stops
				return false;
			}*/
		if ((this.source == null) ? (other.source != null) : !this.source.equals(other.source)) {
			return false;
		}
		if ((this.target == null) ? (other.target != null) : !this.target.equals(other.target)) {
			return false;
		}

		return true;
	}

}
