package com.kleegroup.stages.datavizneo4j.webservices;

import java.util.Collections;
import java.util.Date;

import javax.inject.Inject;

import com.kleegroup.stages.datavizneo4j.services.ImportServices;

import io.vertigo.orchestra.definitions.OrchestraDefinitionManager;
import io.vertigo.orchestra.definitions.ProcessDefinition;
import io.vertigo.orchestra.services.OrchestraServices;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.GET;

public class ImportWebServices implements WebServices {

	@Inject
	private OrchestraServices orchestraServices;
	@Inject
	private OrchestraDefinitionManager orchestraDefinitionManager;
	@Inject
	private ImportServices importServices;

	@AnonymousAccessAllowed
	@GET("/roadanalysis")
	public void importroadanalysisdata() {
		importServices.ImportRoadAnalysisData();
	}

	@AnonymousAccessAllowed
	@GET("/initData")
	public void initData() {
		final ProcessDefinition processDefinition = orchestraDefinitionManager.getProcessDefinition("INIT_DATA");
		orchestraServices.getScheduler().scheduleAt(processDefinition, new Date(), Collections.emptyMap());
	}

	@AnonymousAccessAllowed
	@GET("/initRealTimeData")
	public void initRealTimeData() {
		final ProcessDefinition processDefinition = orchestraDefinitionManager.getProcessDefinition("INIT_REALTIME_DATA");
		orchestraServices.getScheduler().scheduleAt(processDefinition, new Date(), Collections.emptyMap());
	}
}
