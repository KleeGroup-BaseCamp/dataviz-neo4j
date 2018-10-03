package com.kleegroup.stages.datavizneo4j.config;

import javax.inject.Inject;

import com.kleegroup.stages.datavizneo4j.jobs.RealTimeRailwayDataActivityEngine;
import com.kleegroup.stages.datavizneo4j.jobs.RoadFillDataActivityEngine;
import com.kleegroup.stages.datavizneo4j.jobs.TownFillDataActivityEngine;
import com.kleegroup.stages.datavizneo4j.jobs.TransportFillDataActivityEngine;

import io.vertigo.core.component.ComponentInitializer;
import io.vertigo.orchestra.definitions.OrchestraDefinitionManager;
import io.vertigo.orchestra.definitions.ProcessDefinition;
import io.vertigo.orchestra.definitions.ProcessType;

public class OrchestraDefinitionInitializer implements ComponentInitializer {

	@Inject
	private OrchestraDefinitionManager orchestraDefinitionManager;

	@Override
	public void init() {
		final ProcessDefinition processDefinition = ProcessDefinition.builder("INIT_DATA", "Init data")
				.withProcessType(ProcessType.UNSUPERVISED)
				.addActivity("transport", "transport", TransportFillDataActivityEngine.class)
				.addActivity("road", "road", RoadFillDataActivityEngine.class)
				.addActivity("town", "town", TownFillDataActivityEngine.class)
				.build();
		orchestraDefinitionManager.createOrUpdateDefinition(processDefinition);

		final ProcessDefinition processDefinition1 = ProcessDefinition.builder("INIT_REALTIME_DATA", "Init RealTime data")
				.withProcessType(ProcessType.UNSUPERVISED)
				.addActivity("realtime", "realtime", RealTimeRailwayDataActivityEngine.class)
				//.withCronExpression("0 0 0/1 1/1 * ? *")
				.build();
		orchestraDefinitionManager.createOrUpdateDefinition(processDefinition1);
	}

}
