package com.kleegroup.stages.datavizneo4j.webservices;

import java.util.List;

import javax.inject.Inject;

import com.kleegroup.stages.datavizneo4j.domain.StandardDeviation;
import com.kleegroup.stages.datavizneo4j.domain.Town;
import com.kleegroup.stages.datavizneo4j.services.TownServices;

import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.PathPrefix;
import io.vertigo.vega.webservice.stereotype.QueryParam;

@PathPrefix("/Town")
public class TownWebServices implements WebServices {

	@Inject
	private TownServices TServices;

	@AnonymousAccessAllowed
	@GET("/")
	public List<Town> getTowns() {
		return TServices.GetAll();
	}

	@AnonymousAccessAllowed
	@GET("/test")
	public StandardDeviation getVariance(final @QueryParam("insee") String insee) {
		return TServices.variances(insee);
	}

	@AnonymousAccessAllowed
	@GET("/listinsee")
	public List<String> getInseeVariances() {
		return TServices.getinseeforvariances();
	}
}
