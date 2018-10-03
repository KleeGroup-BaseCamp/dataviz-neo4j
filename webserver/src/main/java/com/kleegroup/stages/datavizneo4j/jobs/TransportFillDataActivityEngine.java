package com.kleegroup.stages.datavizneo4j.jobs;

import javax.inject.Inject;

import com.kleegroup.stages.datavizneo4j.services.ImportServices;
import com.kleegroup.stages.datavizneo4j.services.RailwayServices;
import com.kleegroup.stages.datavizneo4j.services.RouteServices;

import io.vertigo.orchestra.services.execution.RunnableActivityEngine;

public class TransportFillDataActivityEngine extends RunnableActivityEngine {

	@Inject
	private RouteServices routeServices;

	@Inject
	private RailwayServices railwayServices;

	@Inject
	private ImportServices importServices;

	@Override
	public void run() {
		importServices.ImportPublicTransportData();
		System.out.println("Static railway data imported");
		railwayServices.RailwayDataConverter();
		System.out.println("Data Conversion ended");
		railwayServices.LdaStopMonitoringRef();
		System.out.println("LdaStop imported");
		importServices.ImportRailwayAnalysisData();
		System.out.println("Railway data analysis imported");
		routeServices.MultiLinesCreation();
		System.out.println("Routes converted");
		importServices.GeoShapeRailway();
		System.out.println("Railway GeoShapes set");
	}

}
