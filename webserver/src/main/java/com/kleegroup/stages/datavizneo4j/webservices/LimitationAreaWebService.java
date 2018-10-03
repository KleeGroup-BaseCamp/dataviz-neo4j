package com.kleegroup.stages.datavizneo4j.webservices;

import java.util.List;

import javax.inject.Inject;

import com.kleegroup.stages.datavizneo4j.domain.LimitationArea;
import com.kleegroup.stages.datavizneo4j.services.LimitationAreaServices;

import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.PathPrefix;

@PathPrefix("/LimitationArea")
public class LimitationAreaWebService implements WebServices {

	@Inject
	private LimitationAreaServices limitationAreaServices;

	@AnonymousAccessAllowed
	@GET("")
	public List<LimitationArea> getLimitationAreas() {
		return limitationAreaServices.getAll();
	}

}
