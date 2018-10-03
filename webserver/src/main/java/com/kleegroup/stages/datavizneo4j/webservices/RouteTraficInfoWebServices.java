package com.kleegroup.stages.datavizneo4j.webservices;

import java.util.List;

import javax.inject.Inject;

import com.kleegroup.stages.datavizneo4j.domain.RouteTraficInfo;
import com.kleegroup.stages.datavizneo4j.services.RouteTraficInfoServices;

import io.vertigo.lang.Tuples.Tuple2;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.PathParam;
import io.vertigo.vega.webservice.stereotype.PathPrefix;

@PathPrefix("/RouteTraficInfo")
public class RouteTraficInfoWebServices implements WebServices {

	@Inject
	private RouteTraficInfoServices routeTraficInfoServices;

	@AnonymousAccessAllowed
	@GET("/RouteTraficInfo/{routeId}")
	public RouteTraficInfo getRouteInfo(final @PathParam("routeId") String routeId) {
		return routeTraficInfoServices.getRoute(routeId);
	}

	@AnonymousAccessAllowed
	@GET("/RouteTraficInfo/Route/{type}")
	public List<Tuple2<String, String>> getRouteNameAndId(final @PathParam("type") int type) {
		return routeTraficInfoServices.getRouteNameAndId(type);
	}

}
