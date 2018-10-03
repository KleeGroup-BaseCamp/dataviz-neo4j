package com.kleegroup.stages.datavizneo4j.services;

import java.util.List;

import com.kleegroup.stages.datavizneo4j.domain.RouteStops;
import com.kleegroup.stages.datavizneo4j.domain.StopsLink;
import com.kleegroup.stages.datavizneo4j.domain.TransportType;

import io.vertigo.core.component.Component;
import io.vertigo.lang.Tuples.Tuple2;

public interface FlowBetweenRoutes extends Component {

	/**
	 * Execute la requête via le Driver Neo4J demandant d'obtenir les données nécessaires pour créer un diagramme
	 * de Sankey montrant l'ensemble du réseau ferroviaire
	 * 	 * @return (Tuple2<List<StopsLink>, List<StopsLink>>) Un tuple de val1 la liste des routeSections entre les différents hubs du graphe et de val2 la liste des routeSections représentant les lignes
	 */
	Tuple2<List<StopsLink>, List<RouteStops>> getFlowBetweenRoutes(final TransportType transportType);

}
