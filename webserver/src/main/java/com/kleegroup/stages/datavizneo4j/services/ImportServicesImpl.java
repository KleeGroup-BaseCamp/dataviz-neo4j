package com.kleegroup.stages.datavizneo4j.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.inject.Inject;

import org.neo4j.driver.v1.Session;

import io.vertigo.core.param.ParamManager;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.ListBuilder;

public class ImportServicesImpl implements ImportServices {

	@Inject
	private Neo4JDriverProvider neo4JDriverProvider;
	@Inject
	private ParamManager paramManager;

	@Override
	public void ImportRoadAnalysisData() {
		final String rootFolder = paramManager.getParam("server.rootFolder").getValueAsString();
		Utilities.sftpscript(rootFolder + "trafic_capteurs_2017_1er_semestre.txt");
		try (Session session = neo4JDriverProvider.getNeo4jDriver().session()) {
			session.run("USING PERIODIC COMMIT load csv with headers from  " +
					" 'file:" + paramManager.getParam("VM.rootFolder").getValueAsString() + "trafic_capteurs_2017_1er_semestre.txt' as csv  " +
					" match (n:RoadSection {id_arc_tra: toInt(csv.id)})-->(m:CatDay{type:csv.type})  " +
					"MERGE (m)-[:FOR_TIMESLOT]->(c:HourSummary {Timeslot:csv.TRNC_HORR}) SET c.occupancy_rate=toFloat(csv.rate),c.flow_rate=toFloat(csv.rateflow),c.speed=toFloat(csv.speed);");
			session.run("MATCH (n:RoadSection)-->(a:CatDay) WHERE NOT (a)-[:FOR_TIMESLOT]->() DETACH DELETE a");
		}
	}

	@Override
	public void ImportRoadData() {
		final String rootFolder = paramManager.getParam("server.rootFolder").getValueAsString();
		final String VMrootFolder = paramManager.getParam("VM.rootFolder").getValueAsString();
		try (BufferedInputStream in = new BufferedInputStream(new URL("https://data.iledefrance.fr/explore/dataset/referentiel-geographique-pour-les-donnees-trafic-issues-des-capteurs-permanents/download/?format=csv&timezone=Europe/Berlin&use_labels_for_header=true").openStream())) {
			final FileOutputStream fout = new FileOutputStream(rootFolder + "referentiel-comptages-routiers.txt");
			final byte data[] = new byte[1024];
			int count;
			while ((count = in.read(data, 0, 1024)) != -1) {
				fout.write(data, 0, count);
			}
			fout.close();
			Utilities.sftpscript(rootFolder + "referentiel-comptages-routiers.txt");
			try (Session session = neo4JDriverProvider.getNeo4jDriver().session()) {
				final List<String> statements = new ListBuilder<String>()
						.add("MATCH (t:Town) DETACH DELETE t")
						.add("USING PERIODIC COMMIT load csv with headers from  " +
								" 'file:" + VMrootFolder + "referentiel-comptages-routiers.txt' as csv FIELDTERMINATOR ';' " +
								" create (r:RoadSection {id:toInt(csv.id_arc),id_arc_tra:toInt(csv.id_arc_tra), geoshape: csv.geo_shape, geopoint: csv.geo_point_2d});")
						.add("create index on :RoadSection(id_arc_tra);")
						.add("match (n:RoadSection) CREATE (n)-[:ON]->(m:CatDay{type:\"JOHV\"})")
						.add("match (n:RoadSection) CREATE (n)-[:ON]->(m:CatDay{type:\"JOVS\"})")
						.add("match (n:RoadSection) CREATE (n)-[:ON]->(m:CatDay{type:\"SAHV\"})")
						.add("match (n:RoadSection) CREATE (n)-[:ON]->(m:CatDay{type:\"SAVS\"})")
						.add("match (n:RoadSection) CREATE (n)-[:ON]->(m:CatDay{type:\"DIJFP\"})")
						.build();
				for (final String statement : statements) {
					session.run(statement);
				}
			}
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}
	}

	@Override
	public void GeoShapeRailway() {
		final String rootFolder = paramManager.getParam("server.rootFolder").getValueAsString();
		Utilities.sftpscript(rootFolder + "MultiLineStrings.txt");
		try (Session session = neo4JDriverProvider.getNeo4jDriver().session()) {
			final List<String> statements = new ListBuilder<String>()
					.add("USING PERIODIC COMMIT  " +
							" LOAD csv WITH HEADERS FROM  " +
							" 'file:" + paramManager.getParam("VM.rootFolder").getValueAsString() + "MultiLineStrings.txt' AS csv FIELDTERMINATOR ';'  " +
							" MATCH (t:Route {id: csv.EXTCODE})  " +
							"SET t.geoshape=csv.`Geo Shape`,t.IDREFLIGC=csv.IDREFLIGC;")
					.build();
			for (final String statement : statements) {
				session.run(statement);
			}
		}
	}

	@Override
	public void ImportRailwayAnalysisData() {
		final String rootFolder = paramManager.getParam("server.rootFolder").getValueAsString();
		final String VMrootFolder = paramManager.getParam("VM.rootFolder").getValueAsString();
		Utilities.sftpscript(rootFolder + "LDA_Stopid.txt");
		Utilities.sftpscript(rootFolder + "reseau_ferré_2017_1er_final.txt");
		try (Session session = neo4JDriverProvider.getNeo4jDriver().session()) {
			final List<String> statements = new ListBuilder<String>()
					.add("match (n:Stop) MERGE (n)-[:ON]->(m:CatDay{type:\"JOHV\"})")
					.add("match (n:Stop) MERGE (n)-[:ON]->(m:CatDay{type:\"JOVS\"})")
					.add("match (n:Stop) MERGE (n)-[:ON]->(m:CatDay{type:\"SAHV\"})")
					.add("match (n:Stop) MERGE (n)-[:ON]->(m:CatDay{type:\"SAVS\"})")
					.add("match (n:Stop) MERGE (n)-[:ON]->(m:CatDay{type:\"DIJFP\"})")
					.add("create index on :Stop(LDA_ID_REF_A)")
					.add("USING PERIODIC COMMIT LOAD CSV WITH HEADERS FROM 'file:" + VMrootFolder + "LDA_Stopid.txt' " +
							"as csv FIELDTERMINATOR ';'" +
							"match (n:Stop{id:csv.stop_id}) SET n.LDA_ID_REF_A=toInt(csv.LDA_REF),n.MonitoringRef=csv.MonitoringRef;")
					.add("USING PERIODIC COMMIT load csv with headers from  " +
							" 'file:" + VMrootFolder + "reseau_ferré_2017_1er_final.txt' as csv FIELDTERMINATOR ';'" +
							" match (n:Stop {LDA_ID_REF_A: toInt(csv.ID_REFA_LDA)})-->(m:CatDay{type:csv.CAT_JOUR})  " +
							" MERGE (m)-[:FOR_TIMESLOT]->(c:HourSummary {Timeslot:csv.TRNC_HORR_60}) SET c.nb_vald=toInt(csv.NB_VALD_MOY)")
					.add("MATCH (a:CatDay) WHERE NOT (a)-[:FOR_TIMESLOT]->() DETACH DELETE a")
					.build();
			for (final String statement : statements) {
				session.run(statement);
			}
		}
	}

	@Override
	public void ImportPublicTransportData() {
		final String rootFolder = paramManager.getParam("server.rootFolder").getValueAsString();
		final String VMrootFolder = paramManager.getParam("VM.rootFolder").getValueAsString();
		final String url_stif = paramManager.getParam("opendata.stif.url").getValueAsString();
		InputStream input = null;
		final StringBuilder fileName = new StringBuilder();
		fileName.append("stif_gtfs.zip");
		try (FileOutputStream writeFile = new FileOutputStream(rootFolder + fileName)) {
			final URL url = new URL(url_stif + "offre-horaires-tc-gtfs-idf/files/f24cf9dbf6f80c28b8edfdd99ea16aad/download/");
			final URLConnection connection = url.openConnection();
			input = connection.getInputStream();
			final byte[] buffer = new byte[1024];
			int read;
			while ((read = input.read(buffer)) > 0) {
				writeFile.write(buffer, 0, read);
			}
			writeFile.flush();
			input.close();
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}
		ZipEntry ze = null;
		try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(rootFolder + fileName.toString())))) {
			while ((ze = zis.getNextEntry()) != null) {
				final File f = new File(rootFolder + "stif_gtfs/" + ze.getName());
				if (ze.isDirectory()) {
					f.mkdirs();
					continue;
				}
				f.getParentFile().mkdirs();
				try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(f))) {
					try {
						final byte[] buf = new byte[8192];
						int bytesRead;
						while (-1 != (bytesRead = zis.read(buf))) {
							fos.write(buf, 0, bytesRead);
						}
					} finally {
						fos.close();
					}
				} catch (final IOException ioe) {
					f.delete();
					throw WrappedException.wrap(ioe);
				}
			}
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		} finally {
			final File file = new File(rootFolder + fileName);
			file.delete();
		}
		final File folder = new File(rootFolder + "stif_gtfs/");
		final File[] listOfFiles = folder.listFiles();
		for (final File file : listOfFiles) {
			Utilities.sftpscript(rootFolder + "stif_gtfs/" + file.getName());
		}
		try (Session session = neo4JDriverProvider.getNeo4jDriver().session()) {
			final List<String> statements = new ListBuilder<String>()
					.add("MATCH (s:Stop) DETACH DELETE s")
					.add("MATCH (t:Trip) DETACH DELETE t")
					.add("create constraint on (t:Trip) assert t.id is unique;")
					.add("create constraint on (s:Stop) assert s.id is unique;")
					.add("create constraint on (a:Agency) assert a.id is unique;")
					.add("create constraint on (r:Route) assert r.id is unique;")
					.add("MATCH (t)-[:EXCEPTION]->(cd:Calendar_Dates) DETACH DELETE cd")
					.add("MATCH (c:Calendar) DETACH DELETE c")
					.add("MATCH (st:Stoptime) DETACH DELETE st")
					.add("MATCH (r:Route) DETACH DELETE r")
					.add("MATCH (a:Agency) DETACH DELETE a")
					.add("LOAD csv WITH HEADERS FROM " +
							" 'file:" + VMrootFolder + "agency.txt' AS csv  " +
							" CREATE (a:Agency {id:toInt(csv.agency_id), name: csv.agency_name, url: csv.agency_url, timezone: csv.agency_timezone});")
					.add("LOAD csv WITH HEADERS FROM " +
							" 'file:" + VMrootFolder + "routes.txt' AS csv  " +
							" MATCH (a:Agency {id: toInt(csv.agency_id)}) " +
							" CREATE (a)-[:OPERATES]->(r:Route {id: csv.route_id, short_name: csv.route_short_name, long_name: csv.route_long_name, type: toInt(csv.route_type)});")
					.add("USING PERIODIC COMMIT LOAD csv WITH HEADERS FROM  " +
							" 'file:" + VMrootFolder + "trips.txt' AS csv  " +
							"match (r:Route {id: csv.route_id})  " +
							" CREATE (r)<-[:USES]-(t:Trip {id: csv.trip_id, service_id: csv.service_id, headsign: csv.trip_headsign, direction_id: csv.direction_id, short_name: csv.trip_short_name, block_id: csv.block_id, bikes_allowed: csv.bikes_allowed, shape_id: csv.shape_id});")
					.add("create index on :Trip(service_id);")
					.add("USING PERIODIC COMMIT LOAD csv WITH HEADERS FROM  " +
							" 'file:" + VMrootFolder + "stops.txt' AS csv  " +
							" CREATE (s:Stop {id: csv.stop_id, name: csv.stop_name, lat: toFloat(csv.stop_lat), lon: toFloat(csv.stop_lon), platform_code: csv.platform_code, parent_station: csv.parent_station, location_type: csv.location_type, timezone: csv.stop_timezone, code: csv.stop_code});")
					.add("create index on :Stop(stop_id);")
					.add("LOAD csv WITH HEADERS FROM  " +
							" 'file:" + VMrootFolder + "stops.txt' AS csv " +
							" WITH csv" +
							" WHERE NOT (csv.parent_station is null)  " +
							" MATCH (ps:Stop {id: csv.parent_station}), (s:Stop {id: csv.stop_id})  " +
							" CREATE (ps)<-[:PART_OF]-(s);")
					.add("USING PERIODIC COMMIT 5000  " +
							" LOAD csv WITH HEADERS FROM  " +
							" 'file:" + VMrootFolder + "stop_times.txt' AS csv  " +
							" MATCH (t:Trip {id: csv.trip_id}), (s:Stop {id: csv.stop_id})  " +
							" CREATE (t)<-[:PART_OF_TRIP]-(st:Stoptime {stop_sequence: toInt(csv.stop_sequence)})-[:LOCATED_AT]->(s) " +
							"SET st.arrival_time=csv.arrival_time,st.departure_time=csv.departure_time;")
					.add("create index on :Stoptime(stop_sequence);")
					.add("USING PERIODIC COMMIT LOAD csv WITH HEADERS FROM  " +
							" 'file:" + VMrootFolder + "calendar.txt' AS csv  " +
							" MATCH (t:Trip {service_id: csv.service_id})  " +
							" CREATE (t)-[:OPEN]->(c:Calendar {monday:csv.monday,tuesday:csv.tuesday,wednesday:csv.wednesday,thursday:csv.thursday,friday:csv.friday,saturday:csv.saturday,sunday:csv.sunday,start_date:csv.start_date,end_date:csv.end_date});")
					.add("USING PERIODIC COMMIT LOAD csv WITH HEADERS FROM " +
							" 'file:" + VMrootFolder + "calendar_dates.txt' AS csv " +
							" MATCH (t:Trip {service_id: csv.service_id}) " +
							" CREATE (t)-[:EXCEPTION]->(cd:Calendar_Dates {service_id: csv.service_id,date:csv.date,exception_type:csv.exception_type});")
					.build();
			for (final String statement : statements) {
				session.run(statement);
			}
		}
	}

	@Override
	public void ImportTownData() {
		final String rootFolder = paramManager.getParam("server.rootFolder").getValueAsString();
		final String VMrootFolder = paramManager.getParam("VM.rootFolder").getValueAsString();
		final String url = paramManager.getParam("opendata.idf.url").getValueAsString();
		try (BufferedInputStream in = new BufferedInputStream(new URL(url + "les-communes-generalisees-dile-de-france/download/?format=csv&timezone=Europe/Berlin&use_labels_for_header=true").openStream())) {
			final FileOutputStream fout = new FileOutputStream(rootFolder + "les-communes-generalisees-dile-de-france.txt");
			final byte data[] = new byte[1024];
			int count;
			while ((count = in.read(data, 0, 1024)) != -1) {
				fout.write(data, 0, count);
			}
			fout.close();
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}
		try (BufferedInputStream in = new BufferedInputStream(new URL(url + "population-francaise-communes-2014/download/?format=csv&timezone=Europe/Berlin&use_labels_for_header=true").openStream())) {
			final FileOutputStream fout = new FileOutputStream(rootFolder + "population-francaise-communes-2014.txt");
			final byte data[] = new byte[1024];
			int count;
			while ((count = in.read(data, 0, 1024)) != -1) {
				fout.write(data, 0, count);
			}
			fout.close();
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}
		Utilities.sftpscript(rootFolder + "population-francaise-communes-2014.txt");
		Utilities.sftpscript(rootFolder + "les-communes-generalisees-dile-de-france.txt");
		try (Session session = neo4JDriverProvider.getNeo4jDriver().session()) {
			final List<String> statements = new ListBuilder<String>()
					.add("MATCH (t:Town) DETACH DELETE t")
					.add("create constraint on (t:Town) assert t.id is unique")
					.add("USING PERIODIC COMMIT load csv with headers from 'file:" + VMrootFolder + "les-communes-generalisees-dile-de-france.txt' as csv FIELDTERMINATOR ';' create (t:Town {id:toInt(csv.insee), name: csv.nomcom,"
							+ " county_nb: csv.NumDep, geopoint: csv.`Geo Point`, geoshape: csv.`Geo Shape`, shape_length: csv.Shape_Leng, shape_area: csv.Shape_Area})")
					.add("USING PERIODIC COMMIT load csv with headers from 'file:" + VMrootFolder + "population-francaise-communes-2014.txt' as csv FIELDTERMINATOR ';'"
							+ " match (t:Town {id: toInt(csv.`Code Insee`)})"
							+ " SET t.total_pop=toInt(csv.`Population totale`)")
					.build();
			for (final String statement : statements) {
				session.run(statement);
			}
		}
	}

}
