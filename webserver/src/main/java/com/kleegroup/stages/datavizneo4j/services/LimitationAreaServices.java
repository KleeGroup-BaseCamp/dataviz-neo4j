package com.kleegroup.stages.datavizneo4j.services;

import java.util.List;

import com.kleegroup.stages.datavizneo4j.domain.LimitationArea;

import io.vertigo.core.component.Component;

public interface LimitationAreaServices extends Component {

	/**
	 * Execute la requête via le Driver Neo4J demandant d'obtenir toutes les données des communes.
	 * 
	 * @return (List<Town>) Communes trouvées
	 */
	List<LimitationArea> getAll();

}
