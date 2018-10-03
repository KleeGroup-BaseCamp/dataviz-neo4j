package com.kleegroup.stages.datavizneo4j.jobs;

import javax.inject.Inject;

import com.kleegroup.stages.datavizneo4j.services.ImportServices;
import com.kleegroup.stages.datavizneo4j.services.RoadSectionServices;

import io.vertigo.orchestra.services.execution.RunnableActivityEngine;

public class RoadFillDataActivityEngine extends RunnableActivityEngine {

	@Inject
	private RoadSectionServices roadSectionServices;

	@Inject
	private ImportServices importServices;

	@Override
	public void run() {
		importServices.ImportRoadData();
		System.out.println("Road importation ended");
		roadSectionServices.DownloadingDatasets(2017);
		System.out.println("Download ended");
		roadSectionServices.firstRegression(2017);
		System.out.println("Regression ended");
		roadSectionServices.completion(2017);
		System.out.println("Completion ended");
		roadSectionServices.roadSectionDataConverter(2017);
		System.out.println("Conversion ended");
		importServices.ImportRoadAnalysisData();
		System.out.println("Road Importation finished");

	}

}
