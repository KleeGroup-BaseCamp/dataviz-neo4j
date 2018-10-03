package com.kleegroup.stages.datavizneo4j.services;

import java.util.List;

import com.kleegroup.stages.datavizneo4j.domain.Route;
import com.kleegroup.stages.datavizneo4j.domain.Stop;

import io.vertigo.core.component.Component;

public interface RailwayServices extends Component {

	/**
	 * Fait une association de deux fichiers .txt. L'un contient le pourcentage de validation par catégorie de jour et par tranche horaire et l'autre contient
	 * le nombre de validations pour tous les types d'abonnements et pour chaque jour de l'année. Crée ensuite un fichier faisant la moyenne des validations
	 * par tranche horaire sur la période de temps du fichier.
	 *
	 */
	void RailwayDataConverter();

	/**
	 * Execute la requête via le Driver Neo4J demandant d'obtenir toutes les données des arrêts (transport en commun) pour une catégorie de jour et une heure
	 * donnée.
	 *
	 * @param 	catDay 	Chaîne de caractères représentant la catégorie de jour ("JOHV","JOVS","SAHV","SAVS","DIJFP")
	 * @param 	hour 	Entier représentant l'heure ( de 0 à 23 )
	 * @return 			(List<top>)  Arrêts trouvées
	 */
	List<Stop> getAllStops(String catDay, int hour);
	
	/**
	 * Execute la requête via le Driver Neo4J demandant d'obtenir toutes les données des arrêts (transport en commun) pour un type de transport donné.
	 * 
	 * @param	route_type	Type de transport (0: Tramway,1: Metro ,2: Train,3: Bus,7: Funiculaire)
	 * @return 			(List<Stop>)  Arrêts trouvées
	 */
	List<Stop> getStopsbyType();

	/**
	 * Execute la requête via le Driver Neo4J demandant d'obtenir toutes les données des arrêts (transport en commun) pour une catégorie de jour et une heure
	 * donnée dans une commune choisie.
	 *
	 * @param 	catDay 	Chaîne de caractères représentant la catégorie de jour ("JOHV","JOVS","SAHV","SAVS","DIJFP")
	 * @param 	hour 	Entier représentant l'heure ( de 0 à 23 )
	 * @param 	townId
	 * @return 			(List<Stop>)  Arrêts trouvées
	 */
	List<Stop> getAllStopsinTown(String catDay, int hour, int townId);

	/**
	 * Analyse trois fichiers pour associer les identifiants ZDE, LDA, Stop_id et MonitoringRef. Crée ensuite un fichier
	 * liant chaque identifiant aux autres.
	 */
	void LdaStopMonitoringRef();

	/**
	 * Execute la requête via le Driver Neo4J demandant d'obtenir toutes les données des lignes (transport en commun).
	 * @return	(List<Sopt>)  Lignes trouvées
	 */
	List<Route> getAllRoutes();

	/**
	 * Fait appel à l'API Stif pour obtenir les informations sur le traffic puis ajoute directement les informations
	 * à la base de donnée pour chaque ligne ou point associé au message.
	 *
	 */
	void infoTraffic();

	/**
	 * Fait appel à l'API Stif pour obtenir les informations sur les prochains passages puis ajoute directement les informations
	 * à la base de donnée pour point concerné.
	 *
	 */
	void infoPassages();
	
	//TODO
	List<Stop> getNextPassages();

	void updatePassagesHistory();

	void getTest();

	void cleanApproach();

	List<Stop> getStopsHistoryByDate(int year, int month, int day);


}
