package com.kleegroup.stages.datavizneo4j.services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.geotools.geojson.geom.GeometryJSON;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import com.kleegroup.stages.datavizneo4j.domain.DateType;
import com.kleegroup.stages.datavizneo4j.domain.Dates;
import com.kleegroup.stages.datavizneo4j.domain.StandardDeviation;
import com.kleegroup.stages.datavizneo4j.domain.Town;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import io.vertigo.core.param.ParamManager;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.ListBuilder;

public class TownServicesImpl implements TownServices {

	@Inject
	private Neo4JDriverProvider neo4JDriverProvider;
	@Inject
	private ParamManager paramManager;

	@Override
	public List<Town> GetAll() {
		final List<Town> result = new ArrayList<>();
		StatementResult rs = null;
		final Driver driver = neo4JDriverProvider.getNeo4jDriver();
		try (Session session = driver.session()) {
			rs = session.run("match (n:Town) RETURN n");
		}
		while (rs.hasNext()) {
			final Record record = rs.next();
			try {
				result.add(new Town(Integer.parseInt(record.get(0).get("id").toString()),
						record.get(0).get("name").toString(),
						Integer.parseInt(record.get(0).get("county_nb").toString().replaceAll("\"", "")),
						Integer.parseInt(record.get(0).get("total_pop").toString()),
						record.get(0).get("geoshape").toString(),
						record.get(0).get("geopoint").toString(), Double.parseDouble(record.get(0).get("shape_area").toString().substring(1, record.get(0).get("shape_area").toString().length() - 1))));
			} catch (final NumberFormatException e) {
				result.add(new Town(Integer.parseInt(record.get(0).get("id").toString()), record.get(0).get("name").toString(), Integer.parseInt(record.get(0).get("county_nb").toString().replaceAll("\"", "")),
						record.get(0).get("geoshape").toString(), record.get(0).get("geopoint").toString()));
			}
		}

		return result;
	}

	@Override
	public void RoadTown() {
		final String rootFolder = paramManager.getParam("server.rootFolder").getValueAsString();
		final String VMrootFolder = paramManager.getParam("VM.rootFolder").getValueAsString();
		final GeometryJSON gjson = new GeometryJSON();
		BufferedReader towns;
		String currenttown;
		try (final PrintWriter writer = new PrintWriter(rootFolder + "road_town.txt", "UTF-8")) {
			writer.println("road,insee");
			final BufferedReader road = new BufferedReader(new FileReader(rootFolder + "referentiel-comptages-routiers.txt"));
			String currentroad = road.readLine();
			while ((currentroad = road.readLine()) != null) {
				towns = new BufferedReader(new FileReader(rootFolder + "les-communes-generalisees-dile-de-france.txt"));
				currenttown = towns.readLine();
				currenttown = towns.readLine();
				try {
					String[] array2 = currenttown.split(";");
					array2[4] = array2[4].replaceAll("\"\"", "\"");
					array2[4] = array2[4].substring(1, array2[4].length());
					final String[] array1 = currentroad.split(";");
					array1[4] = array1[4].replaceAll("\"\"", "\"");
					array1[4] = array1[4].substring(1, array1[4].length());
					final LineString line = gjson.readLine(array1[4]);
					Polygon polygon = gjson.readPolygon(array2[4]);
					if (!polygon.intersects(line)) {
						while (!polygon.intersects(line) && (currenttown = towns.readLine()) != null) {
							try {
								array2 = currenttown.split(";");
								array2[4] = array2[4].replaceAll("\"\"", "\"");
								array2[4] = array2[4].substring(1, array2[4].length());
								polygon = gjson.readPolygon(array2[4]);
							} catch (final Exception e1) {
								// si problème sur la ligne du fichier, on continue la recherche
							}
						}
					}
					writer.println(array1[2] + "," + array2[2]);
				} catch (final Exception e) {
					// si problème de conversion
				}

			}
			road.close();
		} catch (final IOException e2) {
			throw WrappedException.wrap(e2);
		}
		try (Session session = neo4JDriverProvider.getNeo4jDriver().session()) {
			Utilities.sftpscript(rootFolder + "road_town.txt");
			final List<String> statements = new ListBuilder<String>()
					.add("load csv with headers from  " +
							" 'file:" + VMrootFolder + "road_town.txt' as csv  " +
							" match (t:Town {id: toInt(csv.insee)}),(s:RoadSection {id_arc_tra: toInt(csv.road)}) " +
							" MERGE (t)<-[:IN]-(s)")
					.build();
			for (final String statement : statements) {
				session.run(statement);
			}
		}
	}

	@Override
	public void StopTown() {
		final String rootFolder = paramManager.getParam("server.rootFolder").getValueAsString();
		final String VMrootFolder = paramManager.getParam("VM.rootFolder").getValueAsString();
		final GeometryJSON gjson = new GeometryJSON();
		BufferedReader stop;
		String currentstop;
		String currenttown;
		try (final PrintWriter writer = new PrintWriter(rootFolder + "stop_town.txt", "UTF-8")) {
			writer.println("stop_id,insee");
			stop = new BufferedReader(new FileReader(rootFolder + "stif_gtfs/stops.txt"));
			currentstop = stop.readLine();
			while ((currentstop = stop.readLine()) != null) {
				try (final BufferedReader towns = new BufferedReader(new FileReader(rootFolder + "les-communes-generalisees-dile-de-france.txt"))) {
					currenttown = towns.readLine();
					currenttown = towns.readLine();
					String[] array2 = currenttown.split(";");
					array2[4] = array2[4].replaceAll("\"\"", "\"");
					array2[4] = array2[4].substring(1, array2[4].length());
					final String[] array1 = currentstop.split(",");
					final String json = "{\"type\":\"Point\",\"coordinates\":[" + array1[4] + "," + array1[3] + "]}";
					final Reader reader = new StringReader(json);
					final Point p = gjson.readPoint(reader);
					Polygon polygon = gjson.readPolygon(array2[4]);
					if (!polygon.contains(p)) {
						while (!polygon.contains(p) && (currenttown = towns.readLine()) != null) {
							try {
								array2 = currenttown.split(";");
								array2[4] = array2[4].replaceAll("\"\"", "\"");
								array2[4] = array2[4].substring(1, array2[4].length());
								polygon = gjson.readPolygon(array2[4]);
							} catch (final Exception e) {
								//si erreur sur une ligne, on continue la recherche
							}
						}
					}
					writer.println(array1[0] + "," + array2[2]);
				}
			}
			stop.close();
		} catch (final IOException e1) {
			throw WrappedException.wrap(e1);
		}
		try (Session session = neo4JDriverProvider.getNeo4jDriver().session()) {
			Utilities.sftpscript(rootFolder + "stop_town.txt");
			final List<String> statements = new ListBuilder<String>()
					.add("load csv with headers from  " +
							" 'file:" + VMrootFolder + "stop_town.txt' as csv  " +
							" match (t:Town {id: toInt(csv.insee)}),(s:Stop {id: csv.stop_id}) " +
							" MERGE (t)<-[:IN]-(s)")
					.build();
			for (final String statement : statements) {
				session.run(statement);
			}
		}
	}

	public Double Variance(final List<Double> values) {
		Double result = 0.0;
		Double mean = 0.0;
		final Double size = (double) values.size();
		for (final Double value : values) {
			mean += value;
		}
		mean = mean / size;
		for (final Double value : values) {
			final Double a = value - mean;
			result += Math.pow(a, 2);
		}
		return result / size;

	}

	@Override
	public StandardDeviation variances(final String insee) {
		final ArrayList<Double>[] road = new ArrayList[5];
		final ArrayList<Double>[] vald = new ArrayList[5];
		for (int i = 0; i < 5; i++) {
			road[i] = new ArrayList<>();
			vald[i] = new ArrayList<>();
		}

		StatementResult rs1 = null;
		try (Session session = neo4JDriverProvider.getNeo4jDriver().session()) {
			rs1 = session.run("MATCH (n:Town{id:" + insee + "})<--(:RoadSection)-->(c:CatDay)-->(hs:HourSummary) RETURN c.type,hs.flow_rate");
		}
		while (rs1.hasNext()) {
			final Record record1 = rs1.next();
			final Double flow = Double.parseDouble(record1.get(1).toString());
			final String type = record1.get(0).toString();
			final int typenb = Dates.Convert_DateType_int(DateType.valueOf(type.substring(1, type.length() - 1)));
			road[typenb].add(flow);
		}

		try (Session session = neo4JDriverProvider.getNeo4jDriver().session()) {
			rs1 = session.run("MATCH (n:Town{id:" + insee + "})<--(:Stop)-->(c:CatDay)-->(hs:HourSummary) RETURN c.type,hs.nb_vald");
		}
		while (rs1.hasNext()) {
			final Record record1 = rs1.next();
			final String type = record1.get(0).toString();
			final int typenb = Dates.Convert_DateType_int(DateType.valueOf(type.substring(1, type.length() - 1)));
			final Double validation = Double.parseDouble(record1.get(1).toString());
			vald[typenb].add(validation);
		}
		final StandardDeviation std = new StandardDeviation(insee);
		int j = 0;
		for (final List<Double> list : road) {
			final Double variance = Variance(list);
			final Double standardDeviation = Math.sqrt(variance);
			final Double mean = Utilities.CalculateAverageDouble(list);
			std.getRoad()[j] = standardDeviation;
			std.getMoyenne_road()[j] = mean;
			j++;
		}
		j = 0;
		for (final List<Double> list : vald) {
			final Double variance = Variance(list);
			final Double standardDeviation = Math.sqrt(variance);
			final Double mean = Utilities.CalculateAverageDouble(list);
			std.getVald()[j] = standardDeviation;
			std.getMoyenne_vald()[j] = mean;
			j++;
		}

		return std;
	}

	@Override
	public List<String> getinseeforvariances() {
		final List<String> result = new ArrayList<>();
		StatementResult rs1 = null;
		try (Session session = neo4JDriverProvider.getNeo4jDriver().session()) {
			rs1 = session.run("MATCH (n:Town)<--(:RoadSection)-->(:CatDay)-->(:HourSummary) RETURN DISTINCT n");
		}
		while (rs1.hasNext()) {
			final Record record1 = rs1.next();
			result.add(record1.get(0).get("id").toString());
		}
		return result;
	}

}
