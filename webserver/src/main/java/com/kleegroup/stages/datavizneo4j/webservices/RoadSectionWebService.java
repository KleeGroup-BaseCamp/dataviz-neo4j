package com.kleegroup.stages.datavizneo4j.webservices;

import java.util.List;

import javax.inject.Inject;

import com.kleegroup.stages.datavizneo4j.domain.RoadSection;
import com.kleegroup.stages.datavizneo4j.services.RoadSectionServices;

import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.PathParam;
import io.vertigo.vega.webservice.stereotype.PathPrefix;

@PathPrefix("/RoadSection")
public class RoadSectionWebService implements WebServices {

	@Inject
	private RoadSectionServices roadSectionServices;

	@AnonymousAccessAllowed
	@GET("/{cat}/{hour}")
	public List<RoadSection> getRoadSections(final @PathParam("cat") String cat, final @PathParam("hour") int hour) {
		return roadSectionServices.getAll(cat, hour);
	}

	@AnonymousAccessAllowed
	@GET("/Town/{id}/{cat}/{hour}")
	public List<RoadSection> getElementbyTownID(final @PathParam("id") int id, final @PathParam("cat") String cat, final @PathParam("hour") int hour) {
		return roadSectionServices.getRoadsInTown(cat, hour, id);
	}

}
