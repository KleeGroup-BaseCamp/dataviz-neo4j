package com.kleegroup.stages.datavizneo4j.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

public class RouteSectionServicesImpl implements RouteSectionServices {

	@Inject
	private Neo4JDriverProvider neo4JDriverProvider;

	@Override
	public void connectStops() {
		try (final Session session = neo4JDriverProvider.getNeo4jDriver().session()) {

			final HashMap<String, List<String>> routeTrips = new HashMap<>(); // Associe une route à tous les voyages différents qu'elle propose
			final HashMap<String, List<String>> tripStops = new HashMap<>(); // Associe un voyage les arrêts qu'il dessert

			final StatementResult rsRoutes = session.run("MATCH (r:Route) RETURN DISTINCT r.id ");

			// Obtention de tous les voyages
			while (rsRoutes.hasNext()) {
				final Record recordRoutes = rsRoutes.next();

				// Pour chaque route, on mémorise l'ensemble des voyages (sous la forme d'une liste de stops) qu'elle propose
				final StatementResult rsTrips = session.run("MATCH (s:Stop)<--(st:Stoptime)-->(t:Trip{direction_id: '0'})-->(r:Route{id:" + recordRoutes.get(0).toString() + "})\n" +
						"RETURN DISTINCT s.id, r.id, st.stop_sequence, t.id ORDER BY t.id, st.stop_sequence");

				while (rsTrips.hasNext()) {

					final Record recordTrips = rsTrips.next();
					final String route_id = recordTrips.get(1).toString();
					final String stop_id = recordTrips.get(0).toString();
					final String trip_id = recordTrips.get(3).toString();

					// Création d'une nouvelle route
					if (!routeTrips.containsKey(route_id)) {

						// Ajout de l'id du voyage dans la liste des voyages
						final List<String> tripsList = new ArrayList<>();
						tripsList.add(trip_id);
						routeTrips.put(route_id, tripsList);

						// Ajout de l'id du stop dans la liste des stops
						final List<String> stopsList = new ArrayList<>();
						stopsList.add(stop_id);
						tripStops.put(trip_id, stopsList);

					} else {

						final List<String> recordedTrips = routeTrips.get(route_id);

						// Création d'un nouveau trip
						if (!recordedTrips.contains(trip_id)) {

							// Ajout du voyage dans la liste des voyages
							recordedTrips.add(trip_id);
							routeTrips.put(route_id, recordedTrips);

							// Ajout de l'id du stop dans la liste des stops
							final List<String> stopsList = new ArrayList<>();
							stopsList.add(stop_id);
							tripStops.put(trip_id, stopsList);
						}
						// Ajout d'un arrêt au dernier trip
						else {

							final List<String> recordedStops = tripStops.get(trip_id);
							recordedStops.add(stop_id);
							tripStops.put(trip_id, recordedStops);
						}
					}
				}

			}

			// Tri des voyages

			final ArrayList<String> tripsToBeRemoved = findTripsToRemove(tripStops);

			// On enlève les voyages qui sont contenus dans d'autres

			for (final Map.Entry<String, List<String>> pair : routeTrips.entrySet()) {

				tripsToBeRemoved.clear();
				final List<String> trips = pair.getValue();

				for (int i = 0; i < trips.size() - 1; i++) {

					final List<String> listStops_i = tripStops.get(trips.get(i));
					if (listStops_i == null) {
						tripsToBeRemoved.add(trips.get(i));
					} else {
						final List<String> reversed = new ArrayList<>(listStops_i);
						Collections.reverse(reversed);

						for (int j = i + 1; j < trips.size(); j++) {

							final List<String> listStops_j = tripStops.get(trips.get(j));
							if (listStops_i != null && listStops_j != null) {
								if (listStops_i.containsAll(listStops_j) || reversed.containsAll(listStops_j)) {
									tripsToBeRemoved.add(trips.get(j));
								} else if (listStops_j.containsAll(listStops_i) || listStops_j.containsAll(reversed)) {
									tripsToBeRemoved.add(trips.get(i));
								}
							}
						}
					}
				}

				for (final String tripToBeRemoved : tripsToBeRemoved) {
					tripStops.remove(tripToBeRemoved);
					trips.remove(tripToBeRemoved);
				}
				routeTrips.put(pair.getKey().toString(), trips);

			}

			// On enlève les voyages qui sont le sens inverse d'un autre voyage ou qui sont contenus dans un un voyage en  sens inverse
			final Map<String, List<String>> reversedTrips = new HashMap<>();

			for (final Map.Entry<String, List<String>> trip : tripStops.entrySet()) {
				reversedTrips.computeIfAbsent(trip.getKey().toString(), k -> {
					final List<String> reversed = new ArrayList(trip.getValue());
					Collections.reverse(reversed);
					return reversed;
				});
			}

		}
	}

	private static ArrayList<String> findTripsToRemove(final HashMap<String, List<String>> tripStops) {
		final ArrayList<List<String>> distinctTrips = new ArrayList<>();
		final ArrayList<String> tripsToBeRemoved = new ArrayList<>();

		for (final Map.Entry<String, List<String>> pair : tripStops.entrySet()) {
			if (distinctTrips.contains(pair.getValue())) {
				tripsToBeRemoved.add(pair.getKey().toString());
			} else {
				distinctTrips.add(pair.getValue());
			}
		}

		for (final String tripToBeRemoved : tripsToBeRemoved) {
			tripStops.remove(tripToBeRemoved);
		}
		return tripsToBeRemoved;
	}

}
