package com.kleegroup.stages.datavizneo4j.domain;

import java.util.List;

public final class Graph {

	private final List<StopNode> nodes;
	private final List<StopsLink> links;

	public Graph(final List<StopNode> nodes, final List<StopsLink> links) {
		this.nodes = nodes;
		this.links = links;

	}

	public List<StopNode> getNodes() {
		return nodes;
	}

	public List<StopsLink> getLink() {
		return links;
	}

}
