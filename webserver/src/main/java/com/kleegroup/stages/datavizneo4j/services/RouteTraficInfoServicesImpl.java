package com.kleegroup.stages.datavizneo4j.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import com.kleegroup.stages.datavizneo4j.domain.RouteSection;
import com.kleegroup.stages.datavizneo4j.domain.RouteTraficInfo;
import com.kleegroup.stages.datavizneo4j.domain.StopTraficInfo;

import io.vertigo.lang.Tuples;
import io.vertigo.lang.Tuples.Tuple2;
import io.vertigo.lang.VSystemException;
import io.vertigo.vega.engines.webservice.json.UTCDateUtil;

public class RouteTraficInfoServicesImpl implements RouteTraficInfoServices {

	@Inject
	private Neo4JDriverProvider neo4JDriverProvider;

	private enum StopStatus {
		CANCELLED, DELAYED, EARLY, ON_TIME;
	}

	@Override
	public List<Tuple2<String, String>> getRouteNameAndId(final int type) {
		final Driver driver = neo4JDriverProvider.getNeo4jDriver();
		final List<Tuple2<String, String>> namesAndIds = new ArrayList<>();

		final List<Record> rsList;
		try (final Session session = driver.session()) {
			final StatementResult rs = session.run("MATCH(r:Route{type:" + Integer.toString(type) + "})\n" +
					"RETURN r.short_name, r.id");
			rsList = rs.list();
		}

		for (final Record record : rsList) {
			namesAndIds.add(Tuples.of(record.get(0).toString(), record.get(1).toString()));
		}

		return namesAndIds;
	}

	@Override
	public RouteTraficInfo getRoute(final String route_id) {

		final Driver driver = neo4JDriverProvider.getNeo4jDriver();

		float period = 0; // C'est la période sur laquelle les informations sont valides
		//(i.e dateValiditéLaPlusElevée - dateValiditéLaPlusFaible)
		final List<Record> rsList;
		try (final Session session = driver.session()) {
			// On stocke la durée sur laquelle les informations sont valides
			final StatementResult rs = session.run("MATCH(i:Information)\n" +
					"WITH i as info ORDER BY i.validity LIMIT 1\n" +
					"WITH info.validity as minDate\n" +
					"MATCH(i:Information)\n" +
					"WITH minDate, i as info ORDER BY i.validity DESC LIMIT 1\n" +
					"WITH minDate, info.validity AS maxDate\n" +
					"RETURN minDate, maxDate");
			rsList = rs.list();
		}

		for (final Record record : rsList) {
			final String minDateString = record.get(0).toString().replace("\"", "");
			final String maxDateString = record.get(1).toString().replace("\"", "");
			final Date minDate = UTCDateUtil.parse(minDateString);
			final Date maxDate = UTCDateUtil.parse(maxDateString);
			period = maxDate.getTime() - minDate.getTime();
		}

		// Recherche des infos sur les stops
		final Date beforeRequest1 = new Date();
		final List<RouteSection> routeSections = getRouteSections(route_id);
		final List<StopTraficInfo> stopsTraficInfos = new ArrayList<>();
		final HashSet<String> stopsId = new HashSet<>();
		final HashMap<String, String> stopsIdtoName = new HashMap<>();

		for (final RouteSection link : routeSections) {
			stopsId.add(link.getStop1_id());
			stopsId.add(link.getStop2_id());
		}

		int nb_vald = 0;
		for (final String stopId : stopsId) {
			final StopTraficInfo stopTraficInfo = getStopTraficInfo(stopId, period);
			stopsTraficInfos.add(stopTraficInfo);
			stopsIdtoName.put(stopId, stopTraficInfo.getName());
			nb_vald += stopTraficInfo.getNb_vald();
		}
		nb_vald /= 5;

		// On change l'id des routeSections en Name
		final List<RouteSection> routeSectionsWithName = new ArrayList<>();
		for (final RouteSection routeSection : routeSections) {
			routeSectionsWithName.add(new RouteSection(routeSection.getRoute_id(),
					stopsIdtoName.get(routeSection.getStop1_id()),
					stopsIdtoName.get(routeSection.getStop2_id())));
		}

		final Date afterRequest1 = new Date();
		// Recherche du nombre de perturbations moyen par jour

		float nb_disturbances = 0;
		final List<Record> rs2List;
		try (final Session session = driver.session()) {
			final StatementResult rs2 = session.run("MATCH (i:Information)--(r:Route{id:" + route_id + "})"
					+ "RETURN count(i) ");
			rs2List = rs2.list();
		}

		for (final Record record : rs2List) {
			nb_disturbances += record.get(0).asInt();
		}
		nb_disturbances *= 3600 * 1000 * 24 * 7 * 30 / period;

		return new RouteTraficInfo(route_id, nb_vald, nb_disturbances, stopsTraficInfos, routeSectionsWithName);
	}

	private List<RouteSection> getRouteSections(final String routeId) {

		final List<RouteSection> routeSections = new ArrayList<>();

		final Driver driver = neo4JDriverProvider.getNeo4jDriver();
		final List<Record> rsList;
		try (final Session session = driver.session()) {
			final StatementResult rs = session.run(
					"MATCH (s1:StaticStop{route_id: " + routeId + "})-->(s2:StaticStop)\n" +
							"RETURN s1.stop_id, s2.stop_id \n");
			rsList = rs.list();
		}

		for (final Record record : rsList) {
			routeSections.add(new RouteSection(routeId, record.get(0).toString(), record.get(1).toString()));
		}

		return routeSections;

	}

	private StopTraficInfo getStopTraficInfo(final String stopId, final float period) {

		String name = new String();
		int nb_vald = 0;
		int nb_trains = 0;
		float nb_disturbances = 0;
		float perc_delayed = 0;
		float perc_early = 0;
		float perc_cancelled = 0;
		float perc_on_time = 0;
		float perc_unknown = 0;
		float time_delay = 0; // en millisecondes

		final Driver driver = neo4JDriverProvider.getNeo4jDriver();

		// On compte le nombre de trains passant par cet arrêt
		final List<Record> rs0List;
		try (final Session session0 = driver.session()) {
			final StatementResult rs0 = session0.run(
					"MATCH (n:Approach)<--(s:Stop{id:" + stopId + "})\n" +
							"RETURN s.name, count(*)\n");
			rs0List = rs0.list();

			for (final Record record : rs0List) {
				name = record.get(0).toString();
				nb_trains = record.get(1).asInt();
			}
		}

		// On calcule le nombre de validations sur cet arrêt
		final List<Record> rs2List;
		try (final Session session2 = driver.session()) {
			final StatementResult rs2 = session2.run(
					"MATCH (s:Stop{id:" + stopId + "})-->(:CatDay)-->(h:HourSummary)\n" +
							"RETURN SUM(h.nb_vald)");
			rs2List = rs2.list();
		}
		for (final Record record : rs2List) {
			nb_vald = record.get(0).asInt();
		}
		nb_vald /= 5;

		// On calcule le nombre de perturbations sur cet arrêt
		final List<Record> rs3List;

		try (final Session session3 = driver.session()) {
			final StatementResult rs3 = session3.run(
					"MATCH (s:Stop{id:" + stopId + "})--(i:Information)\n" +
							"RETURN count(i)");
			rs3List = rs3.list();
		}
		for (final Record record : rs3List) {
			nb_disturbances += record.get(0).asInt();
		}
		nb_disturbances *= 3600 * 1000 * 24 * 7 * 30 / period;

		// Pas de données sur cet arrêt
		if (nb_trains == 0) {
			try (final Session session0 = driver.session()) {
				final StatementResult rs0 = session0.run(
						"MATCH (s:Stop{id:" + stopId + "})\n" +
								"RETURN s.name\n");
				name = rs0.single().get(0).toString();
			}
		} else {

			// On calcule le pourcentage de trains à l'heure, en avance, en retard et annulés
			for (final StopStatus stopStatus : StopStatus.values()) {
				final Record countByStatus;

				try (final Session session = driver.session()) {
					final StatementResult rs = session.run(
							"MATCH (approach:Approach)<--(s:Stop{id:" + stopId + "})\n" +
									"WHERE approach.arrivalStatus = \"" + stopStatus.name() + "\" \n" +
									"RETURN count(*)\n");
					countByStatus = rs.single();
				}
				switch (stopStatus) {
					case CANCELLED:
						perc_cancelled = ((float) countByStatus.get(0).asInt()) / nb_trains;
						break;
					case DELAYED:
						perc_delayed = ((float) countByStatus.get(0).asInt()) / nb_trains;
						break;
					case EARLY:
						perc_early = ((float) countByStatus.get(0).asInt()) / nb_trains;
						break;
					case ON_TIME:
						perc_on_time = ((float) countByStatus.get(0).asInt()) / nb_trains;
						break;
					default:
						throw new VSystemException("StopStatus '{0}' is not a valid value", stopStatus.name());
				}

				// On calcule le temps de retard moyen d'un train
				final List<Record> rs1List;

				try (final Session session1 = driver.session()) {
					final StatementResult rs1 = session1.run(
							"MATCH (n:Approach)<--(s:Stop{id:" + stopId + "})\n" +
									"WHERE n.arrivalStatus = \"DELAYED\" OR n.arrivalStatus = \"EARLY\"\n" +
									"RETURN n.aimedArrivalTime, n.expectedArrivalTime");
					rs1List = rs1.list();
				}

				for (final Record record : rs1List) {
					final String aimedArrivalTimeString = record.get(0).toString().replace("\"", "");
					final String expectedArrivalTimeString = record.get(1).toString().replace("\"", "");
					final Date aimedArrivalTime = UTCDateUtil.parse(aimedArrivalTimeString);
					final Date expectedArrivalTime = UTCDateUtil.parse(expectedArrivalTimeString);
					time_delay += (aimedArrivalTime.getTime() - expectedArrivalTime.getTime());
				}
				time_delay /= nb_trains;

			}
			perc_unknown = 1 - perc_cancelled - perc_on_time - perc_delayed - perc_early;

		}

		return new StopTraficInfo(name, nb_vald, nb_disturbances, perc_on_time, perc_delayed, perc_early, perc_cancelled, perc_unknown, time_delay);
	}

}
