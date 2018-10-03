package com.kleegroup.stages.datavizneo4j.webservices;

import javax.inject.Inject;

import com.kleegroup.stages.datavizneo4j.services.RouteServices;

import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.PathPrefix;

@PathPrefix("/Route")
public class RouteWebService implements WebServices {

	@Inject
	private RouteServices routeServices;

	@AnonymousAccessAllowed
	@GET("/")
	public void setMultiLineString() {

		routeServices.MultiLinesCreation();
	}
}
