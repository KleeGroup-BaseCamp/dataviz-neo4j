package com.kleegroup.stages.datavizneo4j.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import com.kleegroup.stages.datavizneo4j.domain.Graph;
import com.kleegroup.stages.datavizneo4j.domain.StopNode;
import com.kleegroup.stages.datavizneo4j.domain.StopsLink;

public class StopsGraphServicesImpl implements StopsGraphServices {

	@Inject
	private Neo4JDriverProvider neo4JDriverProvider;

	@Override
	public Graph getGraphByDay(final String catDay) {

		final List<StopNode> nodes = new ArrayList<>(); // Les plus gros arrêts
		final HashMap<String, List<String>> lines = new HashMap<>(); // A une ligne associe les id des stops qui lui sont connectés
		final HashMap<String, Integer> linesVald = new HashMap<>(); // A une ligne associe son nombre de validations
		final HashMap<String, String> routeIdtoName = new HashMap<>();
		final List<StopsLink> links = new ArrayList<>();
		final Driver driver = neo4JDriverProvider.getNeo4jDriver();
		final List<String> routesid = new ArrayList<>();
		final List<Record> rs1List;
		try (Session session = driver.session()) {
			final StatementResult rs1 = session.run("MATCH (s:Stop)<--(sc:Stop)-->(cd:CatDay{type: \"" + catDay + "\"})-->(h:HourSummary) " +
					"WHERE s.id =~\"StopArea.*\" \n" +
					"WITH s AS stop, SUM(h.nb_vald) AS valds ORDER BY valds DESC LIMIT 20 " +
					"MATCH (stop)<--(:Stop)<--(:Stoptime)-->(:Trip)-->(r:Route) " +
					"RETURN DISTINCT stop, valds, r.short_name, r.id");
			rs1List = rs1.list();
		}

		for (final Record record : rs1List) {
			nodes.add(new StopNode(record.get(0).get("name").toString(),
					(float) record.get(0).get("lat").asDouble(),
					(float) record.get(0).get("lon").asDouble(),
					record.get(1).asInt()));

			final String nodeName = record.get(0).get("name").toString();
			final String lineName = record.get(2).toString();

			if (lines.containsKey(lineName)) {
				lines.get(lineName).add(nodeName);
			} else {

				final List<String> node = new ArrayList<>();
				node.add(nodeName);
				lines.put(lineName, node);
				routesid.add(record.get(3).toString());
				routeIdtoName.put(record.get(3).toString(), lineName);
			}

		}

		final List<Record> rs2List;
		try (Session session = driver.session()) {
			final StatementResult rs2 = session.run("UNWIND" + routesid.toString() + " AS routeid \n" +
					"MATCH (ss:StaticStop{route_id: routeid}) \n" +
					"WITH ss.stop_id AS stopid, routeid \n" +
					"MATCH (:Stop{id: stopid})-->(:CatDay{type: \"" + catDay + "\"})-->(h:HourSummary) \n " +
					"WITH routeid, SUM(h.nb_vald) AS route_vald ORDER BY route_vald DESC LIMIT 5\n" +
					"RETURN routeid, route_vald");
			rs2List = rs2.list();
		}

		for (final Record record : rs2List) {
			linesVald.computeIfAbsent(routeIdtoName.get(record.get(0).toString()), k -> record.get(1).asInt());
			linesVald.computeIfPresent(routeIdtoName.get(record.get(0).toString()), (k, v) -> v + record.get(1).asInt());

		}

		final Set<String> mostFrequentedRoutes = linesVald.keySet();
		for (final Map.Entry<String, List<String>> pair : lines.entrySet()) {

			final String routeName = pair.getKey();

			if (mostFrequentedRoutes.contains(routeName)) {
				final List<String> nodesNames = pair.getValue();
				for (int i = 0; i < nodesNames.size() - 1; i++) {
					for (int j = i + 1; j < nodesNames.size(); j++) {

						final StopsLink link = new StopsLink(nodesNames.get(i), nodesNames.get(j), linesVald.get(routeName), routeName);
						if (!links.contains(link) && link.getTarget() != link.getSource()) {
							links.add(new StopsLink(nodesNames.get(i), nodesNames.get(j), linesVald.get(routeName), routeName));
						}
					}
				}
			}
		}

		return new Graph(nodes, links);
	}

}
