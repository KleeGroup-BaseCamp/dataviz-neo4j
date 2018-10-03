package com.kleegroup.stages.datavizneo4j.webservices;

import javax.inject.Inject;

import com.kleegroup.stages.datavizneo4j.domain.Graph;
import com.kleegroup.stages.datavizneo4j.services.StopsGraphServices;

import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.PathParam;
import io.vertigo.vega.webservice.stereotype.PathPrefix;

@PathPrefix("/StopsGraph")
public class StopsGraphWebServices implements WebServices {

	@Inject
	private StopsGraphServices stopGraphServices;

	@AnonymousAccessAllowed
	@GET("/Graph/{catDay}")
	public Graph createGraph(final @PathParam("catDay") String catDay) {
		return stopGraphServices.getGraphByDay(catDay);
	}

}
