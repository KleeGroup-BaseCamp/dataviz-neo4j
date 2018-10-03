package com.kleegroup.stages.datavizneo4j.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import io.vertigo.util.ListBuilder;

// ATTENTION J'AI UNE MAUVAISE BOUCLE SUR LE RER C, AU NIVEAU DES INVALIDES
// POUR LE RER H, J'AI UN LIEN ENTRE PONT PETIT ET PERSAN BEAUMONT GRACE A GARE DU NORD QUI NE DEVRAIT PAS EXISTER
public class StaticRouteSectionServicesImpl implements StaticRouteSectionServices {

	@Inject
	private Neo4JDriverProvider neo4JDriverProvider;

	@Override
	public void createStaticRouteSections() {

		final Driver driver = neo4JDriverProvider.getNeo4jDriver();
		final HashMap<String, List<List<String>>> routeToStaticStops = getMapToRouteSections(driver);
		final List<List<String>> staticStops = mapToStaticStops(routeToStaticStops);

		try (final Session session = neo4JDriverProvider.getNeo4jDriver().session()) {
			final List<String> statements = new ListBuilder<String>()
					.add("MATCH (ss:StaticStop) DETACH DELETE ss")
					.add("UNWIND " + staticStops + "AS rs\n" +
							"MERGE (ss:StaticStop{route_id:rs[0], stop_id: rs[1]})\r\n" +
							"SET ss.id = ss.route_id + ss.stop_id\n" +
							"MERGE (ss2:StaticStop{route_id:rs[0], stop_id: rs[2]})\n" +
							"SET ss2.id = ss2.route_id + ss2.stop_id \n" +
							"WITH ss AS ss, ss2 AS ss2\n" +
							"MATCH (ss), (ss2)\n" +
							"WHERE NOT (ss2)-->(ss) " +
							"MERGE(ss)-[:NEXT_STATICSTOP]->(ss2)\n")
					.build();

			for (final String statement : statements) {
				session.run(statement);
			}

		}

	}

	private static void getRouteTrips(final HashMap<String, HashSet<String>> routeToTrips, final HashMap<String, List<String>> tripToStops, final Driver driver) {
		final List<Record> rsRouteList;

		try (final Session session = driver.session()) {

			final StatementResult rsRoutes = session.run("MATCH (r:Route) RETURN DISTINCT r.id ");
			rsRouteList = rsRoutes.list();

			for (final Record recordRoutes : rsRouteList) {

				final List<Record> rsList;
				final StatementResult rs = session.run("MATCH (s:Stop)<--(st:Stoptime)-->(t:Trip{direction_id: '0'})-->(r:Route{id:" + recordRoutes.get(0).toString() + "})\n" +
						"RETURN s.id, r.id, st.stop_sequence, t.id ORDER BY t.id, st.stop_sequence");
				rsList = rs.list();

				for (final Record record : rsList) {
					final String route_id = record.get(1).toString();
					final String trip_id = record.get(3).toString();
					final String stop_id = record.get(0).toString();

					routeToTrips.computeIfAbsent(route_id, k -> new HashSet<>()).add(trip_id);
					tripToStops.computeIfAbsent(trip_id, k -> new ArrayList<>()).add(stop_id);
				}
			}
		}
	}

	private static HashMap<String, List<List<String>>> getMapToRouteSections(final Driver driver) {

		final HashMap<String, List<List<String>>> routeToStaticRouteSections = new HashMap<>();

		final HashMap<String, HashSet<String>> routeToTrips = new HashMap<>();
		final HashMap<String, List<String>> tripToStops = new HashMap<>();

		getRouteTrips(routeToTrips, tripToStops, driver);

		for (final Map.Entry<String, HashSet<String>> route : routeToTrips.entrySet()) {

			final List<List<String>> routeSections = new ArrayList<>();
			routeSections.add(tripToStops.get(route.getValue().toArray()[0]));

			for (final String trip_id : route.getValue()) {

				final List<String> trip = tripToStops.get(trip_id);
				final List<String> reversed_trip = new ArrayList<>(trip);
				Collections.reverse(reversed_trip);

				boolean useless = false;
				final List<List<String>> toBeRemoved = new ArrayList<>();

				for (int i = 0; i < routeSections.size(); i++) {

					// On insère tous les trips, sauf ceux en sens inverse et ceux déjà contenus dans les routeSections
					final List<String> commonStops = new ArrayList<>(routeSections.get(i));
					commonStops.retainAll(trip);

					if (commonStops.size() > 1) {

						if (routeSections.get(i).containsAll(trip)) {
							useless = true;
						} else if (trip.containsAll(routeSections.get(i))) {
							toBeRemoved.add(routeSections.get(i));
						}

						else if ((trip.indexOf(commonStops.get(1)) - trip.indexOf(commonStops.get(0)))
								* (routeSections.get(i).indexOf(commonStops.get(1)) - routeSections.get(i).indexOf(commonStops.get(0))) > 0) {
							insertStops(routeSections.get(i), trip);
						} else {
							insertStops(routeSections.get(i), reversed_trip);

						}
					}
				}

				if (!useless) {
					routeSections.add(trip);
				}
				routeSections.removeAll(toBeRemoved);

			}
			routeToStaticRouteSections.put(route.getKey(), routeSections);
		}
		return routeToStaticRouteSections;
	}

	/**
	 * Insert les stops manquant de trip2 dans trip1 et inversement
	 * @param trip1
	 * @param trip2
	 */
	private static void insertStops(final List<String> trip1, final List<String> trip2) {

		final List<String> commonStops = new ArrayList<>(trip2);
		commonStops.retainAll(trip1);

		for (int i = 0; i < commonStops.size() - 1; i++) {

			if (trip1.indexOf(commonStops.get(i)) + 1 == trip1.indexOf(commonStops.get(i + 1))
					&& trip2.indexOf(commonStops.get(i)) + 1 < trip2.indexOf(commonStops.get(i + 1))) {

				trip1.addAll(trip1.indexOf(commonStops.get(i)) + 1,
						trip2.subList(trip2.indexOf(commonStops.get(i)) + 1, trip2.indexOf(commonStops.get(i + 1))));

			} else if (trip2.indexOf(commonStops.get(i)) + 1 == trip2.indexOf(commonStops.get(i + 1))
					&& trip1.indexOf(commonStops.get(i)) + 1 < trip1.indexOf(commonStops.get(i + 1))) {

				trip2.addAll(trip2.indexOf(commonStops.get(i)) + 1,
						trip1.subList(trip1.indexOf(commonStops.get(i)) + 1, trip1.indexOf(commonStops.get(i + 1))));

			}
		}

	}

	private static List<List<String>> mapToStaticStops(final HashMap<String, List<List<String>>> map) {

		final List<List<String>> staticStops = new ArrayList<>();

		for (final Map.Entry<String, List<List<String>>> route : map.entrySet()) {

			final String route_id = route.getKey();

			for (final List<String> trip : route.getValue()) {
				for (int i = 0; i < trip.size() - 1; i++) {

					final List<String> routeSection = new ArrayList<>();
					routeSection.add(route_id);
					routeSection.add(trip.get(i));
					routeSection.add(trip.get(i + 1));
					if (!staticStops.contains(routeSection)) {
						staticStops.add(routeSection);
					}
				}
			}
		}

		return staticStops;
	}

}
