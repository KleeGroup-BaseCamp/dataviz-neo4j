package com.kleegroup.stages.datavizneo4j.services;

import java.util.List;

import com.kleegroup.stages.datavizneo4j.domain.RouteTraficInfo;

import io.vertigo.core.component.Component;
import io.vertigo.lang.Tuples.Tuple2;

public interface RouteTraficInfoServices extends Component {

	List<Tuple2<String, String>> getRouteNameAndId(int type);

	/**
	 * Execute la requête via le Driver Neo4J permettant d'obtenir les informations de perturbation du trafic sur une ligne donnée
	 *
	 * @param routeName 	le nom de la ligne sur laquelle on veut des informations
	 * @return (RouteTraficInfo) Les informations sur la ligne
	 */
	RouteTraficInfo getRoute(String routeName);

}
