package com.kleegroup.stages.datavizneo4j.webservices;

import java.util.List;

import javax.inject.Inject;

import com.kleegroup.stages.datavizneo4j.domain.RouteStops;
import com.kleegroup.stages.datavizneo4j.domain.StopsLink;
import com.kleegroup.stages.datavizneo4j.domain.TransportType;
import com.kleegroup.stages.datavizneo4j.services.FlowBetweenRoutes;

import io.vertigo.lang.Tuples.Tuple2;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.PathParam;
import io.vertigo.vega.webservice.stereotype.PathPrefix;

@PathPrefix("/SankeyDiagram")
public class FlowBetweenRoutesWebServices implements WebServices {

	@Inject
	private FlowBetweenRoutes flowRoutesServices;

	@AnonymousAccessAllowed
	@GET("/Sankey/{type}")
	public Tuple2<List<StopsLink>, List<RouteStops>> createSankeyDiagram(final @PathParam("type") int type) {
		final TransportType transportType = TransportType.getTransportTypeFromId(type);
		return flowRoutesServices.getFlowBetweenRoutes(transportType);
	}

}
