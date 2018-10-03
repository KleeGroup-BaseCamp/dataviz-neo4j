package com.kleegroup.stages.datavizneo4j.services;

import java.util.List;

import com.kleegroup.stages.datavizneo4j.domain.StandardDeviation;
import com.kleegroup.stages.datavizneo4j.domain.Town;

import io.vertigo.core.component.Component;

public interface TownServices extends Component {

	/**
	 * Execute la requête via le Driver Neo4J demandant d'obtenir toutes les données des communes.
	 *
	 * @return (List<Town>) Communes trouvées
	 */
	List<Town> GetAll();

	/**
	 * Permet d'associer un arrêt de transport en commun avec la commune dans laquelle il se trouve.
	 * Un fichier est créé avec le code insee de la commune et l'id gtfs de l'arrêt.
	 *
	 */
	void StopTown();

	/**
	 * Permet d'associer une portion de route avec la commune dans laquelle elle se trouve.
	 * Un fichier est créé avec le code insee de la commune et l'id de la section de route.
	 *
	 */
	void RoadTown();

	StandardDeviation variances(String insee);

	List<String> getinseeforvariances();
}
