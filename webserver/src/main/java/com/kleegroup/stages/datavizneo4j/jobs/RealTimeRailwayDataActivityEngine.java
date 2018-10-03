package com.kleegroup.stages.datavizneo4j.jobs;

import javax.inject.Inject;

import com.kleegroup.stages.datavizneo4j.services.RailwayServices;

import io.vertigo.orchestra.services.execution.RunnableActivityEngine;

public class RealTimeRailwayDataActivityEngine extends RunnableActivityEngine {

	@Inject
	private RailwayServices railwayServices;
	
	@Override
	public void run() {
		railwayServices.infoTraffic();
		System.out.println("Trafic Real Time Data Imported");
		railwayServices.infoPassages();
		System.out.println("Next Approachs Imported");
		railwayServices.updatePassagesHistory();
		System.out.println("Histories Updated");
		railwayServices.cleanApproach();
		System.out.println("Approach Cleaned");
	}

}
