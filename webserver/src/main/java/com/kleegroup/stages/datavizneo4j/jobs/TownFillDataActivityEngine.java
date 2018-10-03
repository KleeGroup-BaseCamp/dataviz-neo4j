package com.kleegroup.stages.datavizneo4j.jobs;

import javax.inject.Inject;

import com.kleegroup.stages.datavizneo4j.services.ImportServices;
import com.kleegroup.stages.datavizneo4j.services.TownServices;

import io.vertigo.orchestra.services.execution.RunnableActivityEngine;

public class TownFillDataActivityEngine extends RunnableActivityEngine {

	@Inject
	private ImportServices importServices;

	@Inject
	private TownServices townServices;

	@Override
	public void run() {
		importServices.ImportTownData();
		System.out.println("Town Initialized");
		townServices.RoadTown();
		System.out.println("Town/Road relations created");
		townServices.StopTown();
		System.out.println("Town/Stop relations created");
	}

}
