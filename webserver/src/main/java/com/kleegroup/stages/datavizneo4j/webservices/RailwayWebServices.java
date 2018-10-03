package com.kleegroup.stages.datavizneo4j.webservices;

import java.util.List;

import javax.inject.Inject;

import com.kleegroup.stages.datavizneo4j.domain.Route;
import com.kleegroup.stages.datavizneo4j.domain.Stop;
import com.kleegroup.stages.datavizneo4j.services.RailwayServices;

import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.PathParam;
import io.vertigo.vega.webservice.stereotype.PathPrefix;
import io.vertigo.vega.webservice.stereotype.QueryParam;

@PathPrefix("/Stop")
public class RailwayWebServices implements WebServices {

	@Inject
	private RailwayServices railwayServices;

	@AnonymousAccessAllowed
	@GET("/initData")
	public void initData() {
		railwayServices.RailwayDataConverter();
	}

	@AnonymousAccessAllowed
	@GET("/Route")
	public List<Route> getRailways() {
		return railwayServices.getAllRoutes();
	}

	@AnonymousAccessAllowed
	@GET("/")
	public List<Stop> getAllStops(final @QueryParam("cat") String cat, final @QueryParam("hour") int hour) {
		return railwayServices.getAllStops(cat, hour);
	}

	@AnonymousAccessAllowed
	@GET("/Train_Metro")
	public List<Stop> getStopsbyType() {
		return railwayServices.getStopsbyType();
	}

	@AnonymousAccessAllowed
	@GET("/Day_History")
	public List<Stop> getStopsbyDay(final @QueryParam("year") int year, final @QueryParam("month") int month, final @QueryParam("day") int day) {
		return railwayServices.getStopsHistoryByDate(year, month, day);
	}

	@AnonymousAccessAllowed
	@GET("/NextPassages")
	public List<Stop> getStopsNextPassages() {
		return railwayServices.getNextPassages();
	}

	@AnonymousAccessAllowed
	@GET("/Town/{id}/{cat}/{hour}")
	public List<Stop> getElementbyTownID(final @PathParam("id") int id, final @PathParam("cat") String cat, final @PathParam("hour") int hour) {
		return railwayServices.getAllStopsinTown(cat, hour, id);
	}

}
