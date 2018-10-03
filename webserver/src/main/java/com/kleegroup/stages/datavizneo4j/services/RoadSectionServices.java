package com.kleegroup.stages.datavizneo4j.services;

import java.io.IOException;
import java.util.List;

import com.kleegroup.stages.datavizneo4j.domain.RoadSection;

import io.vertigo.core.component.Component;

public interface RoadSectionServices extends Component {

	/**
	 * Permet de lire chaque fichier ( un nom standardisé est requis ) qui contient les informations
	 * concernant les débits et les taux d'occupations des sections de routes sur Paris. Un fichier est ensuite créé
	 * contenant les moyennes des débits, taux d'occupations et vitesses pour chaque section et pour chaque type
	 * jour (JOHV,JOVS,SAHV,SAVS,DIJFP). Chaque type de jour est divisé en créneau horaire de une heure.
	 *
	 * @param 	year 	Entier représentant l'année choisie
	 */
	void roadSectionDataConverter(int year);

	/**
	 * Permet de calculer le nombre de route pour un mois et une année renseigné.
	 *
	 * @param 	year 	Entier représentant l'année choisie
	 * @param 	month 	Entier représentant le mois choisi
	 * @return 			(Integer) Nombre de route trouvé.
	 */
	int nbRoadSectionMonth(int year, int month);

	/**
	 * Permet de trouver le nombre de section de route différent en analysant le nombre d'id différents
	 * dans les fichiers
	 *
	 * @param 	year 	Entier représentant l'année choisie
	 * @return 			(Integer) Le maximun de section de route sur l'ensemble des fichiers
	 */
	int nbRoadSection(int year);

	/**
	 * Permet de calculer la vitesse sur une section de route.
	 *
	 * @param 		rate		Le taux d'occupation relevé sur la section
	 * @param 		rateflow	Le débit relevé sur le section
	 * @param 		distance	La longueur d'une véhicule en moyenne + La longueur du capteur au sol
	 * @return					(Double) La vitesse calculée
	 */
	double calculateSpeed(Double rate, Double rateflow, Double distance);

	/**
	 * Execute la requête via le Driver Neo4J demandant d'obtenir toutes les données des routes pour une catégorie de jour et une heure
	 * donnée.
	 *
	 * @param 	catDay 	Chaîne de caractères représentant la catégorie de jour ("JOHV","JOVS","SAHV","SAVS","DIJFP")
	 * @param 	hour 	Entier représentant l'heure ( de 0 à 23 )
	 * @return 			(List<RoadSection>)  Routes trouvées
	 */
	List<RoadSection> getAll(String catDay, int hour);

	/**
	 * Execute la requête via le Driver Neo4J demandant d'obtenir toutes les données des routes pour une catégorie de jour et une heure
	 * donnée dans une commune choisie.
	 *
	 * @param 	catDay 	Chaîne de caractères représentant la catégorie de jour ("JOHV","JOVS","SAHV","SAVS","DIJFP")
	 * @param 	hour 	Entier représentant l'heure ( de 0 à 23 )
	 * @param 	townId
	 * @return 			(List<RoadSection>)  Routes trouvées
	 */
	List<RoadSection> getRoadsInTown(String catDay, int hour, int townId);

	/**
	 * Télécharge le fichier .zip contenant les fichiers .txt des données sur les sections de route de Paris. Dézippe
	 * le fichier et sauvegarde les fichiers .txt dans un dossier.
	 *
	 * @param 	year 	Entier représentant l'année choisie ( 2013 à 2018 disponible )
	 */
	void DownloadingDatasets(int year);

	/**
	 * Crée un fichier avec pour chaque section de route, catégorie de jour et créneau horaire, les coeffecients directeurs des
	 * régressions polynomiales du deuxième et troisième ordre des valeurs correspondantes. Les regressions sont calculées seulement si le nombre de couple
	 * de valeurs est supérieur à 20.
	 *
	 * @param year Entier représentant l'année choisie ( 2013 à 2018 disponible )
	 */
	void firstRegression(int year);

	/**
	 * Pour chaque ligne des données brutes, si il manque le débit, récupère les données des régressions si
	 * elles existent et complete les données puis écrit un fichier avec toutes les lignes complétées.
	 *
	 * @param year Entier représentant l'année choisie ( 2013 à 2018 disponible )
	 */
	void completion(int year);

	public void LimitationAreaDataConverter() throws IOException;

	/** Crée un fichier contenant les id des routes et des zones 30 correspondantees
	 * @throws IOException
	 */
	void connectRoadtoLimitationAreas() throws IOException;

}
