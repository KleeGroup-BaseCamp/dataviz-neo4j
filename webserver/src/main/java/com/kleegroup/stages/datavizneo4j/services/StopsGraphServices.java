package com.kleegroup.stages.datavizneo4j.services;

import com.kleegroup.stages.datavizneo4j.domain.Graph;

import io.vertigo.core.component.Component;

public interface StopsGraphServices extends Component {

	/**
	 * Execute la requête via le Driver Neo4J demandant d'obtenir les données nécessaires pour créer un graphe
	 * comportant les noeuds les plus fréquentés lors du type de jour choisi
	 *
	 * @param catDay 	Chaîne de caractères représentant la catégorie de jour ("JOHV","JOVS","SAHV","SAVS","DIJFP")
	 * @return (Graph) Le graphe trouvé
	 */
	Graph getGraphByDay(String catDay);
}
