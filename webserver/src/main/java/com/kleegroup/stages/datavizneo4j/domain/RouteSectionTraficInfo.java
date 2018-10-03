package com.kleegroup.stages.datavizneo4j.domain;

public final class RouteSectionTraficInfo {

	private final StopTraficInfo source;
	private final StopTraficInfo target;

	public RouteSectionTraficInfo(final StopTraficInfo source, final StopTraficInfo target) {
		this.source = source;
		this.target = target;
	}

	public StopTraficInfo getSource() {
		return source;
	}

	public StopTraficInfo getTarget() {
		return target;
	}
}
