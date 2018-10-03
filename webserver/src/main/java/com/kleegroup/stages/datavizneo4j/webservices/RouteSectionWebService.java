package com.kleegroup.stages.datavizneo4j.webservices;

import java.util.Date;

import javax.inject.Inject;

import com.kleegroup.stages.datavizneo4j.services.RouteSectionServices;
import com.kleegroup.stages.datavizneo4j.services.StaticRouteSectionServices;

import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.PathPrefix;

@PathPrefix("/RouteSection")
public class RouteSectionWebService implements WebServices {

	@Inject
	private RouteSectionServices routeSectionServices;
	@Inject
	private StaticRouteSectionServices staticRouteSectionServices;

	@AnonymousAccessAllowed
	@GET("/")
	public void setStopsConnections() {
		final Date beforeRequest = new Date();
		routeSectionServices.connectStops();
		final Date afterRequest = new Date();
		System.out.print("Durée de la requête (ms) : " + (afterRequest.getTime() - beforeRequest.getTime()));
	}

	@AnonymousAccessAllowed
	@GET("/StaticRouteSections")
	public void setStaticRouteSections() {
		final Date beforeRequest = new Date();
		staticRouteSectionServices.createStaticRouteSections();
		final Date afterRequest = new Date();
		System.out.print("Durée de la requête (ms) : " + (afterRequest.getTime() - beforeRequest.getTime()));
	}

}
