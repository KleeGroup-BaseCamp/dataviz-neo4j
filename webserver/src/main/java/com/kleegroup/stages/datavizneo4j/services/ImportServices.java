package com.kleegroup.stages.datavizneo4j.services;

import io.vertigo.core.component.Component;

public interface ImportServices extends Component {

	/**
	 * Déplace le fichier .txt concernant les données sur les routes vers le dossier 'Import' et exécute la commande d'importation.
	 * 
	 * @param Path Chemin d'accès au dossier 'Import' dans les fichiers Neo4J.
	 */
	void ImportRoadAnalysisData();
	
	void ImportRoadData();

	/**
	 * Télécharge le répertoire GTFS sur les transport en commun en Ile-de-France, le dézippe et exécute les commandes
	 * d'importation dans la base de donnée.
	 * 
	 */
	void ImportPublicTransportData();

	/**
	 * Télécharge le fichier concernant les populations des communes (population-francaise-communes-2014)
	 * 
	 */
	void ImportTownData();

	/**
	 * Envoie des données vers la VM via SFTP puis exécution des requêtes d'importations :
	 * - Création des CatDay pour les stops
	 * - Ajout des LDA_ID et des références de Monitoring pour chaque arrêt.
	 * - Ajout des données de validations pour chaque CatDay
	 */
	void ImportRailwayAnalysisData();

	/**
	 * Envoie le fichier des tracés des lignes de transport ferré en SFTP et exécute
	 * la requête qui permet d'associer chaque ligne à son tracé.
	 */
	void GeoShapeRailway();
}
