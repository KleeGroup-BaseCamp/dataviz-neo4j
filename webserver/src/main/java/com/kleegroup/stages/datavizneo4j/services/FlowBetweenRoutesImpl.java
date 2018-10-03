package com.kleegroup.stages.datavizneo4j.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import com.kleegroup.stages.datavizneo4j.domain.NodeFromRouteSection;
import com.kleegroup.stages.datavizneo4j.domain.RouteStops;
import com.kleegroup.stages.datavizneo4j.domain.StopsLink;
import com.kleegroup.stages.datavizneo4j.domain.TransportType;

import io.vertigo.lang.Tuples;
import io.vertigo.lang.Tuples.Tuple2;
import io.vertigo.lang.VUserException;

public class FlowBetweenRoutesImpl implements FlowBetweenRoutes {

	@Inject
	private Neo4JDriverProvider neo4JDriverProvider;

	@Override
	public Tuple2<List<StopsLink>, List<RouteStops>> getFlowBetweenRoutes(final TransportType type) {

		final List<String> beginningRouteSectionsId = new ArrayList<>(); // Stocke l'id des routeSections démarrant une route
		final HashMap<String, String> endRouteSections = new HashMap<>(); // On a besoin ici de l'id du stop correspondant au stop de fin
		final HashMap<String, NodeFromRouteSection> nodes = new HashMap<>(); // Contient les informations relatives à un noeud : id, name et route_section_id
		final HashMap<String, String> routeIdtoName = new HashMap<>(); // Associe l'id d'une route à son short_name
		final HashMap<String, List<String>> trips = new HashMap<>(); // Associe l'id d'un trip (c'est en réalité l'id de la routeSection qui l'a commencé) à l'id de tous les arrêts qu'elle contient et qui seront affichés
		final HashMap<String, String> tripIdtoRouteId = new HashMap<>(); // Associe l'id d'un trip à la route à laquelle il appartient
		final Driver driver = neo4JDriverProvider.getNeo4jDriver();

		initializeGraph(driver, type, beginningRouteSectionsId, endRouteSections, routeIdtoName, trips, tripIdtoRouteId, nodes);
		constructTrips(driver, type, beginningRouteSectionsId, endRouteSections, trips, nodes);
		filterTrips(driver, type, trips, nodes, tripIdtoRouteId);
		final List<StopsLink> links = createSankeyDiagramLinks(driver, type, trips, nodes, tripIdtoRouteId, routeIdtoName);

		cleanLinks(links, type);

		final List<RouteStops> routes = createRouteStops(links);
		return Tuples.of(links, routes);

	}

	/**
	 * Exécute la requête via le Driver Neo4j permettant d'obtenir les routeSections de début et fin de trips (i.e d'itinéraires de lignes)
	 * ainsi que la plupart des informations utiles à la construction du diagramme
	 * @param type le type de transport choisi
	 * @param beginningRouteSectionsId une liste qui contient, après exécution de la méthode, les id de toutes les RouteSections commençant un trip
	 * @param endRouteSections une map qui, après exécution de la méthode, associe l'id d'un trip à son stop de fin
	 * @param routeIdtoName une map qui, après exécution de la méthode, associe l'id d'une route (i.e ligne) à son nom (short_name)
	 * @param trips une map qui, après exécution de la méthode, associe l'"id" du trip (c'est en réalité l'id de la première routeSection du trip)
	 * à une liste de routeSections (stopsLink) consistant en les hubs du trip qui seront affichés sur le diagramme
	 * @param tripIdtoRouteId une map qui, après exécution de la méthode, associe l'"id" du trip au nom de la route qu'il utilise
	 * @param nodes une map qui, après exécution de la requête, associe l'id d'un arrêt de début de trip aux informations qui lui sont relatives (nom, nombre de validations)
	 */
	private static void initializeGraph(final Driver driver, final TransportType type, final List<String> beginningRouteSectionsId, final HashMap<String, String> endRouteSections, final HashMap<String, String> routeIdtoName, final HashMap<String, List<String>> trips, final HashMap<String, String> tripIdtoRouteId, final HashMap<String, NodeFromRouteSection> nodes) {
		final List<Record> rsList;
		try (final Session session = driver.session()) {

			// Renvoie toutes les routesSection de début et de fin de ligne
			final StatementResult rs = session.run(
					// On ne prend que les lignes de bus/tram/métro...
					"MATCH(r:Route{type: " + Integer.toString(type.getTypeStored()) + "}) \n" +
							"WITH r.id AS routeId, r.short_name AS routeName \n" +
							"MATCH (rs:RouteSection{route_id:routeId})<-[:PART_OF_ROUTESECTION{stop_sequence:0}]-(:Stop)-->(saBegin:Stop) \n" +
							"WITH rs AS rsBegin, routeName,  routeId, saBegin AS saBegin \n" +
							"MATCH (rsBegin)-[:NEXT_ROUTESECTION*]->(route_section:RouteSection)-[part_of:PART_OF_ROUTESECTION]-(s:Stop) \n" +
							"WITH max(part_of.stop_sequence) AS nb_max, rsBegin, routeName, routeId, saBegin \n" +
							"MATCH (rsBegin)-[:NEXT_ROUTESECTION*]->(route_section:RouteSection)<-[part_of:PART_OF_ROUTESECTION]-(:Stop) \n" +
							"WHERE part_of.stop_sequence = nb_max \n" +
							"WITH rsBegin, route_section AS rsEnd, routeName, saBegin \n" +
							"MATCH (saEnd:Stop)<--(:Stop)-->(rsEnd) \n" +
							"RETURN saBegin, rsBegin, saEnd, rsEnd, routeName");
			rsList = rs.list();
		}
		for (final Record record : rsList) {

			final String routeId = record.get(1).get("route_id").toString();
			final String stopIdBegin = record.get(1).get("stop1_id").toString();
			final String stopNameBegin = record.get(0).get("name").toString();
			final String stopIdEnd = record.get(3).get("stop2_id").toString();
			final String stopNameEnd = record.get(2).get("name").toString();
			final String routeSectionBegin = record.get(1).get("id").toString();
			final String routeName = record.get(4).toString();

			beginningRouteSectionsId.add(routeSectionBegin);
			endRouteSections.put(routeSectionBegin, stopIdEnd);

			// Remplissage de la map routes ; on ne met que le stop de début, on ajoutera le stop de fin après tous les autres pour conserver l'ordre
			trips.computeIfAbsent(routeSectionBegin, k -> {
				return new ArrayList<>();
			}).add(stopIdBegin);

			tripIdtoRouteId.computeIfAbsent(routeSectionBegin, k -> routeId);
			routeIdtoName.computeIfAbsent(routeId, k -> routeName);
			nodes.computeIfAbsent(stopIdBegin, k -> new NodeFromRouteSection(stopNameBegin, routeSectionBegin, 0));
			nodes.computeIfAbsent(stopIdBegin, k -> new NodeFromRouteSection(stopNameEnd, routeSectionBegin, 0));
		}

	}

	/**
	 * Exécute la requête via le Driver Neo4j permettant de construire tous les trips utilisés par le type de transport choisi
	 * Si ce type est le bus (3), train (2) ou métro (1) (?), on ne prend que les arrêts les plus fréquentés pour accélérer l'obtention des données
	 * @param type le type de transport choisi
	 * @param beginningRouteSectionsId l'id des routeSections commençant les trips parcourus
	 * @param endRouteSections la map qui associe un id de trip à son stop de fin et permet d'ajouter ce stop au trip considéré en tout dernier
	 * @param trips une map qui, à la fin de la méthode, associe l'id d'un trips à l'ensemble des hubs qu'il contient, ordonnés par ordre de passage
	 * @param nodes une map, qui, à la fin de la méthode, associe l'id d'un stop du graphe aux informations qui lui sont relatives (nom, nombre de validations)
	 */
	private static void constructTrips(final Driver driver, final TransportType type, final List<String> beginningRouteSectionsId, final HashMap<String, String> endRouteSections, final HashMap<String, List<String>> trips, final HashMap<String, NodeFromRouteSection> nodes) {
		final List<Record> rsList;
		try (final Session session = driver.session()) {
			final StatementResult rs;

			if (type == TransportType.TRAMWAY || type == TransportType.FUNICULAIRE) {
				rs = session.run(
						"UNWIND " + beginningRouteSectionsId + " AS route_begin_id " +
						// A partir de tous ces premiers maillons, on peut remonter le long de la route
								"MATCH (rs:RouteSection{id:route_begin_id})-[:NEXT_ROUTESECTION*]->(route_section:RouteSection)<-[part_of:PART_OF_ROUTESECTION]-(s:Stop)-->(sa:Stop)\n" +
								"USING INDEX rs:RouteSection(id) \n" +
								"WHERE sa.id =~\"StopArea.*\" \n" +
								"WITH  sa AS stop_area, route_section.route_id AS route_id, s.id AS stop_id, route_begin_id\n" + // + ORDER BY PART_OF_ROUTESECTION et on peut virer les stops begin et stops end
								"MATCH (stop_area)<--(s:Stop) \n" +
								"WITH stop_area,  count(s) AS count_s, route_id, stop_id,  route_begin_id\n" +
								"MATCH (stop_area)\n" +
								"WHERE count_s > 1 \n" +
								"RETURN route_id, stop_id, route_begin_id, stop_area");

				rsList = rs.list();
			} else {
				rs = session.run("UNWIND " + beginningRouteSectionsId + " AS route_begin_id " +
				// A partir de tous ces premiers maillons, on peut remonter le long de la route
						"MATCH (rs:RouteSection{id:route_begin_id})-[:NEXT_ROUTESECTION*]->(route_section:RouteSection)<-[part_of:PART_OF_ROUTESECTION]-(s:Stop)-->(sa:Stop)\r\n" +
						"USING INDEX rs:RouteSection(id)\n" +
						"WHERE sa.id =~\"StopArea.*\" \r\n" + // à mon avis, on veut plutôt rs.id
						"WITH  sa AS stop_area, route_section.route_id AS route_id, s.id AS stop_id, route_begin_id\n" + // + ORDER BY PART_OF_ROUTESECTION et on peut virer les stops begin et stops end
						"MATCH (stop_area)<--(s:Stop) \n" +
						"WITH stop_area,  count(s) AS count_s, route_id, stop_id, route_begin_id\n" +
						"MATCH (stop_area)\n" +
						"WHERE count_s > 1 \n" +
						"WITH stop_area, route_id, stop_id, route_begin_id\n" +
						"MATCH (h:HourSummary)<--(:CatDay)<--(:Stop)-->(stop_area)\n" +
						" WITH SUM(h.nb_vald) AS nb_valds, route_id, stop_id, route_begin_id, stop_area\n" +
						"RETURN route_id, stop_id, route_begin_id, stop_area, nb_valds");
				rsList = rs.list();

			}

			for (final Record record : rsList) {
				final String routeSectionId = record.get(2).toString();
				final String stopId = record.get(1).toString();
				final String stopName = record.get(3).get("name").toString();
				int nb_valds;
				if (type != TransportType.TRAMWAY & type != TransportType.FUNICULAIRE) {
					nb_valds = record.get(4).asInt();
				} else {
					nb_valds = 0;
				}

				// Stockage de l'id du hub dans la route dont il fait partie
				trips.computeIfPresent(routeSectionId, (k, v) -> {
					if (!v.contains(stopId)) {
						v.add(stopId);
					}
					return v;
				});
				nodes.computeIfAbsent(stopId, k -> new NodeFromRouteSection(stopName, routeSectionId, nb_valds));

			}

			// Ajout des stops de fin, pour être sûr qu'ils seront bien à la dernière position
			for (final Map.Entry<String, String> stop_end : endRouteSections.entrySet()) {

				trips.computeIfPresent(stop_end.getKey(), (k, v) -> {
					v.add(stop_end.getValue());
					return v;
				});
			}
		}

	}

	/**
	 * Trie les trips de façon à ne garder
	 * dans le cas du bus, du train ou du métro : que les arrêts étant reliés à au moins deux routes, et les plus fréquentés, et les trips contenant plus de deux arrêts
	 * dans le cas du funiculaire ou du tramway : que les arrêts étant reliés à au moins deux trips
	 * @param type le type de transport choisi
	 * @param trips la liste de tous les trips affichés dans le Sankey Diagram
	 * @param nodes une map contenant les informations relatives aux arrêts traversés
	 * @param tripIdtoRouteId
	 */
	private void filterTrips(final Driver driver, final TransportType type, final HashMap<String, List<String>> trips, final HashMap<String, NodeFromRouteSection> nodes, final HashMap<String, String> tripIdtoRouteId) {

		// On enlève d'abord les arrêts les moins fréquentés, puis les trips qui n'ont que deux stops
		if (type == TransportType.METRO || type == TransportType.TRAIN || type == TransportType.BUS) {

			int nb_nodes = 0;
			switch (type) {
				case METRO: // Métro
					nb_nodes = 50;
					break;
				case TRAIN: // Trains
					nb_nodes = 25;
					break;
				case BUS: // Bus
					nb_nodes = 30;
					break;
				default:
					throw new VUserException("Type : '{0}' is not a supported transport type", type);
			}

			final ArrayList<Integer> nb_valds = new ArrayList<>();
			for (final Map.Entry<String, NodeFromRouteSection> node : nodes.entrySet()) {
				nb_valds.add(node.getValue().getNb_vald());
			}
			Collections.sort(nb_valds);

			// C'est le nombre de validations minimum souhaité
			final int lim = nb_valds.get(nb_valds.size() - nb_nodes);

			// Puis on trie et on prend la valeur telle qu'on ne garde que nb_nodes stops
			final List<String> tripsToBeRemoved = new ArrayList<>();
			for (final Map.Entry<String, List<String>> trip : trips.entrySet()) {
				final List<String> stops = trip.getValue();
				final List<String> stopsToBeRemoved = new ArrayList<>();

				if (stops.size() > 2) {
					for (int i = 1; i < stops.size() - 1; i++) {
						if (nodes.get(stops.get(i)).getNb_vald() < lim) {
							stopsToBeRemoved.add(stops.get(i));
						}
					}
					for (final String stopToBeRemoved : stopsToBeRemoved) {
						if (!stopToBeRemoved.equals(stops.get(0)) && !stopToBeRemoved.equals(stops.get(stops.size() - 1))) { // Attention, s'il y a plusieurs copies du noeud initial, elles vont rester
							stops.remove(stopToBeRemoved);
						}
					}
				}

				// On supprime les trips qui n'ont que deux noeuds
				final String tripId = trip.getKey().toString();
				if ((new HashSet<>(stops)).size() == 2) {
					tripsToBeRemoved.add(tripId);
				} else {
					trips.put(tripId, stops);
				}
			}

			for (final String tripId : tripsToBeRemoved) {
				trips.remove(tripId);
			}
		}

		// Tri des hubs : on ne garde que ceux qui sont connectés à deux routes différentes pour le type de transport choisi
		final Map<String, Set<String>> reversedIndex = new HashMap<>();
		final Map<String, Set<String>> reversedIndexTripId = new HashMap<>();

		trips.entrySet().stream()
				.flatMap(entry -> entry.getValue().stream().map(value -> Tuples.of(value, entry.getKey())))
				.forEach(tuple -> {
					if (nodes.get(tuple.getVal1()) != null) {
						reversedIndex.computeIfAbsent(nodes.get(tuple.getVal1()).getStop_name(), k -> new HashSet<>()).add(tripIdtoRouteId.get(tuple.getVal2()));
					}
					reversedIndexTripId.computeIfAbsent(tuple.getVal1(), k -> new HashSet<>()).add(tuple.getVal2());

				});
		for (final Map.Entry<String, List<String>> trip : trips.entrySet()) {

			final List<String> stops = trip.getValue();
			final List<String> newStops = new ArrayList<>();
			if (stops.size() > 2) {
				newStops.add(stops.get(0));
				for (int i = 1; i < stops.size() - 1; i++) {

					if (reversedIndex.get(nodes.get(stops.get(i)).getStop_name()).size() > 1 && !newStops.contains(stops.get(i))) {
						newStops.add(stops.get(i));
					}
				}
				if (!newStops.contains(stops.get(stops.size() - 1))) {
					newStops.add(stops.get(stops.size() - 1));
				}
				trips.put(trip.getKey().toString(), newStops);
			}
		}
	}

	/**
	 * Trouve tous les stops de début de ligne parmis les routeSections
	 * @param links toutes les routeSections
	 * @return (List<StopsLink>) la liste des stops de début de ligne
	 */
	private static List<StopsLink> findBeginningLinks(final List<StopsLink> links) {
		final List<StopsLink> beginningLinks = new ArrayList<>();
		for (final StopsLink link : links) {
			if (link.previousLink(links) == null) {
				beginningLinks.add(link);
			}
		}

		return beginningLinks;
	}

	/**
	 * Crée une map qui, à une routeSection du graphe associe la liste des routeSections qui la suivent directement
	 * @param links toutes les routeSections
	 * @return (Map<StopsLink, List<StopsLink>>) ladite map
	 */
	private static Map<StopsLink, List<StopsLink>> getFollowingLinks(final List<StopsLink> links) {
		final Map<StopsLink, List<StopsLink>> followingLinks = new HashMap<>();
		for (final StopsLink link : links) {
			for (final StopsLink otherLink : links) {
				if (!link.equals(otherLink) && otherLink.follows(link)) {
					followingLinks.computeIfAbsent(link, k -> new ArrayList<>()).add(otherLink);
				}
			}
		}

		return followingLinks;
	}

	/**
	 * Supprime la routeSection links de l'ensemble des routeSections de façon à ne faire de "trous" dans les routes
	 * @param link la rotueSections à supprimer
	 * @param links toutes les routeSections
	 */
	private static void removeLink(final StopsLink link, final List<StopsLink> links) {

		final StopsLink nextLink = link.nextLink(links);
		if (nextLink != null) {
			links.add(new StopsLink(link.getSource(), nextLink.getTarget(), nextLink.getTotalValds() + link.getTotalValds(), link.getName()));
			links.remove(link);
			links.remove(nextLink);
		}
	}

	/**
	 * Supprime toutes les boucles présentes dans les routeSections
	 * i.e dès qu'on a stop0 -> ... -> stop0, on supprime la routeSection à l'origine de cette boucle
	 * @param links toutes les routeSections
	 */
	private static void removeLoops(final List<StopsLink> links) {
		final List<StopsLink> beginningLinks = findBeginningLinks(links);
		final List<List<StopsLink>> beginningRoutes = new ArrayList<>();
		final Map<StopsLink, List<StopsLink>> followingLinks = getFollowingLinks(links);
		final List<StopsLink> toBeRemoved = new ArrayList<>();

		for (final StopsLink beginningLink : beginningLinks) {
			final List<StopsLink> begin = new ArrayList<>();
			begin.add(beginningLink);
			beginningRoutes.add(begin);
		}
		findLinksToBeRemovedFromLink(toBeRemoved, beginningRoutes, followingLinks);

		for (final StopsLink link : toBeRemoved) {
			removeLink(link, links);
		}

	}

	/**
	 * Fonction auxiliaire de removeLoops définie par récurrence
	 * @param toBeRemoved la list des stopsLink à supprimer (car à l'origine de boucles)
	 * @param registeredRoutes tous les chemins possibles du début du graphe à la fin du graphe
	 * @param followingLinks la map qui à un stopsLink associe tous les stopsLinks qui le suivent directement
	 */
	private static void findLinksToBeRemovedFromLink(final List<StopsLink> toBeRemoved, final List<List<StopsLink>> registeredRoutes, final Map<StopsLink, List<StopsLink>> followingLinks) {

		final List<List<StopsLink>> routesToBeAdded = new ArrayList<>();
		for (final List<StopsLink> route : registeredRoutes) {
			final StopsLink link = route.get(route.size() - 1);
			final List<StopsLink> followingLinksList = followingLinks.get(link);

			if (followingLinksList != null) {
				for (final StopsLink nextLink : followingLinksList) {

					//boolean isAlreadyRegistered = false;
					//					for (final List<StopsLink> registeredRoute : registeredRoutes) {
					//						if (registeredRoute.contains(nextLink)) {
					//							isAlreadyRegistered = true;
					//						}
					//					}
					if (!route.contains(nextLink)) {

						//		if (!isAlreadyRegistered) {
						final List<StopsLink> newRoute = new ArrayList<>(route);
						newRoute.add(nextLink);
						routesToBeAdded.add(newRoute);
					} else {
						//	if (route.contains(nextLink)) {
						toBeRemoved.add(link);
					}
				}
			}
		}

		if (routesToBeAdded.size() != 0) {
			findLinksToBeRemovedFromLink(toBeRemoved, routesToBeAdded, followingLinks);
		}
	}

	/**
	 * Une fois les boucles supprimées, cette fonction supprime les routeSections connectées à des stops isoés (i.e qui ne sont traversés que par une seule route)
	 * @param links
	 */
	private static void cleanLinks(final List<StopsLink> links, final TransportType type) {
		//	removeLoops(links);
		final List<StopsLink> listOfLinks = new ArrayList<>();
		for (final StopsLink link : links) {
			listOfLinks.add(link);
		}
		for (final StopsLink link : listOfLinks) {
			if (links.contains(link)) {
				if (type == TransportType.TRAIN || type == TransportType.METRO || type == TransportType.BUS) {
					removeUselessSection(link, links, type);
				}
			}
		}

	}

	static void removeUselessSection(final StopsLink link, final List<StopsLink> allLinks, final TransportType type) {

		// Relié à une seule route
		final Set<String> routesOfSource = new HashSet<>();
		final Set<String> routesOfTarget = new HashSet<>();
		routesOfSource.add(link.getName());
		routesOfTarget.add(link.getName());

		StopsLink previousRouteSection = link;
		StopsLink nextRouteSection = link;
		int connectedToSource = 0;
		int connectedToTarget = 0;

		for (final StopsLink otherLink : allLinks) {
			if ((otherLink.getTarget()).equals(link.getSource())) {
				routesOfSource.add(otherLink.getName());
				previousRouteSection = otherLink;

				if (otherLink.getName().equals(link.getName())) {
					connectedToSource++;
				}

			} else if ((otherLink.getSource()).equals(link.getTarget())) {
				routesOfTarget.add(otherLink.getName());
				nextRouteSection = otherLink;

				if (otherLink.getName().equals(link.getName())) {
					connectedToTarget++;
				}
			}
		}

		// la source de ce link n'est reliée qu'à une route, elle est donc inutile et peut être supprimée
		if (routesOfSource.size() == 1 && connectedToSource != 0) {
			allLinks.remove(link);
			if (!(previousRouteSection.getSource().equals(link.getSource()))) {
				allLinks.add(new StopsLink(previousRouteSection.getSource(), link.getTarget(), previousRouteSection.getTotalValds() + link.getTotalValds(), previousRouteSection.getName()));
			}
		}
		if (routesOfTarget.size() == 1 && connectedToTarget != 0) {

			if (!nextRouteSection.getTarget().equals(link.getTarget())) {
				allLinks.add(new StopsLink(link.getSource(), nextRouteSection.getTarget(), nextRouteSection.getTotalValds() + link.getTotalValds(), nextRouteSection.getName()));
			}
			allLinks.remove(link);
		}

	}

	/**
	 * Crée un objet stockant le nom des routes (lignes) et tous ses stops de début et de fin
	 * @param links les liens entre les stations qui vont être affichées par le Sankey Diagram
	 * @return RouteStops associant à chaque nom de ligne, les stops de début et de fin qui lui sont associés
	 */
	static List<RouteStops> createRouteStops(final List<StopsLink> links) {

		final List<RouteStops> routes = new ArrayList<>();

		for (final StopsLink link : links) {

			int connectedToSource = 0;
			int connectedToTarget = 0;
			for (final StopsLink otherLink : links) {
				if (!otherLink.equals(link)) {
					if (otherLink.getTarget().equals(link.getSource())) {
						if (otherLink.getName().equals(link.getName())) {
							connectedToSource++;
						}
					} else if (otherLink.getSource().equals(link.getTarget())) {
						if (otherLink.getName().equals(link.getName())) {
							connectedToTarget++;
						}
					}
				}
			}
			if (connectedToSource == 0) {

				boolean is_registered = false;
				for (final RouteStops route : routes) {
					if (route.getRouteName().equals(link.getName())) {
						route.addBeginningStop(link.getSource());
						is_registered = true;
						break;
					}
				}
				if (!is_registered) {
					final List<String> sources = new ArrayList<>();
					sources.add(link.getSource());
					routes.add(new RouteStops(link.getName(), sources, new ArrayList<String>()));
				}

			}
			if (connectedToTarget == 0) {
				boolean is_registered = false;
				for (final RouteStops route : routes) {
					if (route.getRouteName().equals(link.getName())) {
						route.addEndStop(link.getTarget());
						is_registered = true;
						break;
					}
				}
				if (!is_registered) {
					final List<String> targets = new ArrayList<>();
					targets.add(link.getTarget());
					routes.add(new RouteStops(link.getName(), new ArrayList<String>(), targets));
				}
			}
		}

		return routes;
	}

	/**
	 * A partir des données obtenues, crée la liste des StopsLink constituant le Sankey Diagram
	 * @param type
	 * @param trips
	 * @param nodes
	 * @param tripIdtoRouteId
	 * @param routeIdtoName
	 */
	private static List<StopsLink> createSankeyDiagramLinks(final Driver driver, final TransportType type, final HashMap<String, List<String>> trips,
			final HashMap<String, NodeFromRouteSection> nodes, final HashMap<String, String> tripIdtoRouteId, final HashMap<String, String> routeIdtoName) {

		final List<List<String>> routeSections = new ArrayList<>(); // Contient des listes de la forme [nom_route, nom_hub_1, nom_hub_2, id_route_section]
		final List<StopsLink> links = new ArrayList<>(); // Contient la liste des liens affichés dans le diagramme
		final Date beforeCreate = new Date();

		// Stockage de toutes les routeSections formant le Sankey diagram
		for (final Map.Entry<String, List<String>> pair : trips.entrySet()) {

			final String routeId = tripIdtoRouteId.get(pair.getKey());
			final List<String> nodesIds = pair.getValue();

			if (((type == TransportType.BUS || type == TransportType.TRAIN) && nodesIds.size() > 2)
					|| (type != TransportType.BUS/* && type != TransportType.METRO*/)) {
				for (int i = 0; i < nodesIds.size() - 1; i++) {

					final List<String> routeSection = new ArrayList<>();
					routeSection.add(routeId);
					routeSection.add(nodesIds.get(i));
					routeSection.add(nodesIds.get(i + 1));
					routeSection.add(nodes.get(nodesIds.get(i)).getRouteSection_id());
					routeSections.add(routeSection);
				}
			}

		}

		final List<Record> rsList;
		try (final Session session = driver.session()) {
			final StatementResult rs = session.run("UNWIND " + routeSections.toString() + " AS route_section\n" +
					"MATCH p = (rs_begin:RouteSection{id: route_section[3], stop1_id: route_section[1]})-[:NEXT_ROUTESECTION*]->(rs_end:RouteSection{stop2_id:route_section[2]})\n" +
					"USING INDEX rs_begin:RouteSection(id)\n" +
					"WITH  nodes(p) AS nodes, route_section[0] AS route_id, route_section[1] AS stop_id_begin, route_section[2] AS stop_id_end\n" +
					"UNWIND nodes AS node\n" +
					"MATCH (node)<--(:Stop)-->(:CatDay)-->(h:HourSummary)\n" +
					"RETURN route_id, stop_id_begin, stop_id_end, SUM(h.nb_vald)");

			rsList = rs.list();
		}

		for (final Record record : rsList) {
			// Ajout des links dont le nombre de validations est connu
			final NodeFromRouteSection nodeInfo_begin = nodes.get(record.get(1).toString());
			final NodeFromRouteSection nodeInfo_end = nodes.get(record.get(2).toString());
			final StopsLink link = new StopsLink(nodeInfo_begin.getStop_name(), nodeInfo_end.getStop_name(), record.get(3).asInt(), routeIdtoName.get(record.get(0).toString()));
			if (!link.getSource().equals(link.getTarget()) && !links.contains(link) && !link.hasOppositeLinkInList(links)) {
				links.add(link);
			}
		}
		// Ajout des links dont le nombre de validation est inconnu
		for (final List<String> triplet : routeSections) {

			if (nodes.get(triplet.get(1)) != null && nodes.get(triplet.get(2)) != null) {
				final NodeFromRouteSection nodeInfo_begin = nodes.get(triplet.get(1));
				final NodeFromRouteSection nodeInfo_end = nodes.get(triplet.get(2));
				final StopsLink link = new StopsLink(nodeInfo_begin.getStop_name(), nodeInfo_end.getStop_name(), 0, routeIdtoName.get(triplet.get(0)));

				if (!links.contains(link) && !link.getSource().equals(link.getTarget()) && !link.hasOppositeLinkInList(links)) {
					links.add(link);
				}
			}
		}
		return links;
	}

}
