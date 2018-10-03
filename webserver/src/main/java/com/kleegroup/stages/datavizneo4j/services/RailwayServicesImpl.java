package com.kleegroup.stages.datavizneo4j.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import com.kleegroup.stages.datavizneo4j.domain.Approach;
import com.kleegroup.stages.datavizneo4j.domain.DateType;
import com.kleegroup.stages.datavizneo4j.domain.Dates;
import com.kleegroup.stages.datavizneo4j.domain.HourSummary;
import com.kleegroup.stages.datavizneo4j.domain.Route;
import com.kleegroup.stages.datavizneo4j.domain.Stop;

import io.vertigo.core.param.ParamManager;
import io.vertigo.lang.WrappedException;

public class RailwayServicesImpl implements RailwayServices {

	@Inject
	private static Neo4JDriverProvider neo4JDriverProvider;
	@Inject
	private ParamManager paramManager;

	@Override
	public void RailwayDataConverter() {
		final String url = paramManager.getParam("opendata.idf.url").getValueAsString();
		final String rootFolder = paramManager.getParam("server.rootFolder").getValueAsString();
		final Map<Integer, Integer> mapid = new TreeMap<>();
		int current_value = 0;
		try (BufferedReader rates = new BufferedReader(new FileReader(rootFolder + "validations-sur-le-reseau-ferre-profils-horaires-par-jour-type-1er-sem.txt"))) {
			Utilities.downloadfile(new URL(url + "validations-sur-le-reseau-ferre-nombre-de-validations-par-jour-1er-semestre-2017/download/?format=csv&timezone=Europe/Berlin&use_labels_for_header=true"), rootFolder + "validations-sur-le-reseau-ferre-nombre-de-validations-par-jour-1er-sem.txt");
			Utilities.downloadfile(new URL(url + "validations-sur-le-reseau-ferre-profils-horaires-par-jour-type-1er-semestre-2017/download/?format=csv&timezone=Europe/Berlin&use_labels_for_header=true"), rootFolder + "validations-sur-le-reseau-ferre-profils-horaires-par-jour-type-1er-sem.txt");
			String line = rates.readLine();
			while ((line = rates.readLine()) != null) {
				final String[] array1 = line.split(";");
				final int id = Integer.parseInt(array1[4]);
				if (!mapid.containsKey(id)) {
					mapid.put(id, current_value);
					current_value++;
				}
			}
			rates.close();
		} catch (final IOException e1) {
			throw WrappedException.wrap(e1);
		}
		final Double[][][] values = new Double[mapid.size()][5][24];
		String rates_line = null;
		try (BufferedReader rates = new BufferedReader(new FileReader(rootFolder + "validations-sur-le-reseau-ferre-profils-horaires-par-jour-type-1er-sem.txt"))) {
			while ((rates_line = rates.readLine()) != null) {
				final String[] array1 = rates_line.split(";");
				final int int_type = Dates.Convert_DateType_int(DateType.valueOf(array1[5]));
				try {
					final Integer value = mapid.get(Integer.parseInt(array1[4]));
					if (value != null) {
						final String[] horr = array1[6].split("H-");
						values[value][int_type][Integer.parseInt(horr[0])] = Double.parseDouble(array1[7]);
					}
				} catch (final Exception e) {
					// si problème sur la ligne, on continues
				}
			}
			rates.close();
		} catch (final IOException e1) {
			throw WrappedException.wrap(e1);
		}
		BufferedReader origin = null;
		String origin_line = null;
		try (PrintWriter result = new PrintWriter(rootFolder + "reseau_ferré_2017_1er1.txt", "UTF-8")) {
			origin = new BufferedReader(new FileReader(rootFolder + "validations-sur-le-reseau-ferre-nombre-de-validations-par-jour-1er-sem.txt"));
			result.println("JOUR;CODE_STIF_TRNS;CODE_STIF_RES;CODE_STIF_ARRET;LIBELLE_ARRET;ID_REFA_LDA;CAT_JOUR;TRNC_HORR_60;CATEGORIE_TITRE;NB_VALD");
			origin_line = origin.readLine();
			String[] array1 = origin_line.split(";");
			while ((origin_line = origin.readLine()) != null) {
				try {
					array1 = origin_line.split(";");
					final SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
					Date t;
					t = ft.parse(array1[0]);

					final DateType type = Dates.DateType(t);
					final int int_type = Dates.Convert_DateType_int(type);
					if (!array1[5].isEmpty()) {
						final Integer value = mapid.get(Integer.parseInt(array1[5]));
						for (int i = 0; i < 24; i++) {
							try {
								final Double pourc = values[value][int_type][i];
								String horr;
								if (i == 23) {
									horr = i + "H-0H";
								} else {
									horr = i + "H-" + (i + 1) + "H";
								}
								if (!array1[7].equals("Moins de 5")) {
									final Double nb_val = (pourc / 100 * Integer.parseInt(array1[7]));
									int nbValDone;
									if (nb_val > 0) {
										nbValDone = nb_val.intValue() + 1;
									} else {
										nbValDone = 0;
									}
									result.println(array1[1] + ";" + array1[2] + ";" + array1[3] + ";" + array1[4] + ";" + array1[5] + ";" + horr + ";" + type + ";" + array1[6] + ";" + nbValDone);
								} else {
									result.println(array1[1] + ";" + array1[2] + ";" + array1[3] + ";" + array1[4] + ";" + array1[5] + ";" + horr + ";" + type + ";" + array1[6] + ";Moins de 5");
								}
							} catch (final Exception e) {
								//si problème sur la ligne, on continue
							}
						}
					}
				} catch (final Exception e) {
					//ne rien faire
				}
			}
			origin.close();
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}

		@SuppressWarnings("unchecked")
		final List<Integer>[][][] values1 = new ArrayList[mapid.size()][5][24];
		for (int i = 0; i < mapid.size(); i++) {
			for (int j = 0; j < 5; j++) {
				for (int k = 0; k < 24; k++) {
					values1[i][j][k] = new ArrayList<>();
				}
			}
		}
		try (PrintWriter result = new PrintWriter(rootFolder + "reseau_ferré_2017_1er_final.txt", "UTF-8")) {
			origin = new BufferedReader(new FileReader(rootFolder + "reseau_ferré_2017_1er1.txt"));
			origin_line = origin.readLine();
			while ((origin_line = origin.readLine()) != null) {
				final String[] array1 = origin_line.split(";");
				final String[] horr = array1[5].split("H-");
				final int int_type = Dates.Convert_DateType_int(DateType.valueOf(array1[6]));
				final Integer value = mapid.get(Integer.parseInt(array1[4]));
				if (array1[8].equals("Moins de 5")) {
					values1[value][int_type][Integer.parseInt(horr[0])].add(3);
				} else {
					values1[value][int_type][Integer.parseInt(horr[0])].add(Integer.parseInt(array1[8]));
				}
			}
			origin.close();
			result.println("ID_REFA_LDA;CAT_JOUR;TRNC_HORR_60;NB_VALD_MOY");
			for (int i = 0; i < mapid.size(); i++) {
				for (int j = 0; j < 5; j++) {
					for (int k = 0; k < 24; k++) {
						final int mean = Utilities.CalculateAverageInteger(values1[i][j][k]);
						if (k == 23) {
							result.println(Utilities.getKeyFromValue(mapid, i) + ";" + Dates.Convert_int_DateType(j) + ";" + k + "H-0H;" + mean);
						} else {
							result.println(Utilities.getKeyFromValue(mapid, i) + ";" + Dates.Convert_int_DateType(j) + ";" + k + "H-" + (k + 1) + "H;" + mean);
						}
					}
				}
			}
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}
	}

	@Override
	public void cleanApproach() {
		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		final Driver driver = neo4JDriverProvider.getNeo4jDriver();
		try (Session session = driver.session()) {
			session.run("MATCH (a:Approach) WHERE a.expectedDepartureTime=\"\" AND a.expectedArrivalTime=\"\" AND a.aimedArrivalTime=\"\" AND a.aimedDepartureTime=\"\"  DETACH DELETE a");
			final StatementResult rs = session.run("MATCH (a:Approach) RETURN a,ID(a)");
			final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			while (rs.hasNext()) {
				final Record record = rs.next();
				final String aimedArrivalTime = record.get(0).get("aimedArrivalTime").toString();
				final String expectedArrivalTime = record.get(0).get("expectedArrivalTime").toString();
				final String expectedDepartureTime = record.get(0).get("expectedDepartureTime").toString();
				final String aimedDepartureTime = record.get(0).get("aimedDepartureTime").toString();

				final String id = record.get(1).toString();
				if (!expectedArrivalTime.equals("\"\"")) {
					Date date2;
					try {
						date2 = format.parse(expectedArrivalTime.substring(1, expectedArrivalTime.length() - 1));
						if (date2.before(calendar.getTime())) {
							session.run("MATCH (a:Approach) WHERE ID(a)=" + id + " DETACH DELETE a");
						}
					} catch (final ParseException e) {
						//on continue
					}
				}
				if (!expectedDepartureTime.equals("\"\"")) {
					Date date2;
					try {
						date2 = format.parse(expectedDepartureTime.substring(1, expectedDepartureTime.length() - 1));
						if (date2.before(calendar.getTime())) {
							session.run("MATCH (a:Approach) WHERE ID(a)=" + id + " DETACH DELETE a");
						}
					} catch (final ParseException e) {
						//on continue
					}
				}
				if (!aimedArrivalTime.equals("\"\"")) {
					try {

						final Date date1 = format.parse(aimedArrivalTime.substring(1, aimedArrivalTime.length() - 1));
						if (date1.before(calendar.getTime())) {
							session.run("MATCH (a:Approach) WHERE ID(a)=" + id + " DETACH DELETE a");
						}
					} catch (final ParseException e) {
						//on continue
					}
				}
				if (!aimedDepartureTime.equals("\"\"")) {
					try {
						final Date date1 = format.parse(aimedDepartureTime.substring(1, aimedDepartureTime.length() - 1));
						if (date1.before(calendar.getTime())) {
							session.run("MATCH (a:Approach) WHERE ID(a)=" + id + " DETACH DELETE a");
						}
					} catch (final ParseException e) {
						//on continue
					}
				}

			}
		}

	}

	@Override
	public List<Stop> getStopsbyType() {
		final Driver driver = neo4JDriverProvider.getNeo4jDriver();
		try (Session session = driver.session()) {
			final StatementResult rs = session.run("MATCH (p:RouteSection)<--(s:Stop)-->(a:Approach), (r:Route{id:p.route_id}) WHERE s.id CONTAINS \"Point\" AND EXISTS(s.lat) AND EXISTS(s.lon) AND EXISTS(s.name) AND (r.type=2 OR r.type=1) RETURN DISTINCT s");

			return rs.list()
					.stream()
					.map(RailwayServicesImpl::buildStopbis)
					.collect(Collectors.toList());
		}
	}

	private static Stop buildStopbis(final Record record) {
		return new Stop(Double.parseDouble(record.get(0).get("lat").toString()), Double.parseDouble(record.get(0).get("lon").toString()), record.get(0).get("id").toString(), record.get(0).get("name").toString(), Integer.parseInt(record.get(0).get("location_type").toString().replaceAll("\"", "")));
	}

	@Override
	public List<Stop> getStopsHistoryByDate(final int year, final int month, final int day) {
		final Driver driver = neo4JDriverProvider.getNeo4jDriver();
		try (Session session = driver.session()) {
			final StatementResult rs = session.run("MATCH (d:Day{day:" + day + ",month:" + month + ",year:" + year + "})<-[r:DAY_SUM]-(s:Stop) return s,r");

			return rs.list()
					.stream()
					.map(RailwayServicesImpl::buildStopDay)
					.collect(Collectors.toList());
		}
	}

	private static Stop buildStopDay(final Record record) {
		int ontime = 0;
		int delays = 0;
		int cancel = 0;
		if (!record.get(1).get("ontime").isNull()) {
			ontime = Integer.parseInt(record.get(1).get("ontime").toString());
		}
		if (!record.get(1).get("delays").isNull()) {
			delays = Integer.parseInt(record.get(1).get("delays").toString());
		}
		if (!record.get(1).get("cancel").isNull()) {
			cancel = Integer.parseInt(record.get(1).get("cancel").toString());
		}
		return new Stop(Double.parseDouble(record.get(0).get("lat").toString()), Double.parseDouble(record.get(0).get("lon").toString()), record.get(0).get("id").toString(), record.get(0).get("name").toString(), Integer.parseInt(record.get(0).get("location_type").toString().replaceAll("\"", "")),
				ontime, delays, cancel);
	}

	private static Approach nextApproach(final String id) {
		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		final Driver driver = neo4JDriverProvider.getNeo4jDriver();
		Approach nextapp = new Approach("", "", "", "", "", "", "");
		final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		try (Session session = driver.session()) {
			final Date next = null;
			final StatementResult approachs = session.run("Match (s:Stop{id:" + id + "})-->(a:Approach) RETURN a");
			while (approachs.hasNext()) {
				final Record approach = approachs.next();
				final String aimedArrivalTime = approach.get(0).get("aimedArrivalTime").toString();
				final String expectedArrivalTime = approach.get(0).get("expectedArrivalTime").toString();
				final String expectedDepartureTime = approach.get(0).get("expectedDepartureTime").toString();
				final String aimedDepartureTime = approach.get(0).get("aimedDepartureTime").toString();
				if (!expectedArrivalTime.substring(1, expectedArrivalTime.length() - 1).equals("")) {
					try {
						final Date date1 = format.parse(expectedArrivalTime.substring(1, expectedArrivalTime.length() - 1));
						if (next != null && !date1.before(calendar.getTime())) {
							if (!date1.before(next)) {
								nextapp = new Approach(approach.get(0).get("vehicleRef").toString(), approach.get(0).get("aimedArrivalTime").toString(), approach.get(0).get("aimedDepartureTime").toString(), approach.get(0).get("arrivalStatus").toString(), approach.get(0).get("destinationRef").toString(), approach.get(0).get("expectedArrivalTime").toString(), approach.get(0).get("expectedDepartureTime").toString());
							}
						} else {
							nextapp = new Approach(approach.get(0).get("vehicleRef").toString(), approach.get(0).get("aimedArrivalTime").toString(), approach.get(0).get("aimedDepartureTime").toString(), approach.get(0).get("arrivalStatus").toString(), approach.get(0).get("destinationRef").toString(), approach.get(0).get("expectedArrivalTime").toString(), approach.get(0).get("expectedDepartureTime").toString());
						}
					} catch (final ParseException e) {
						//on continue
					}
				} else if (!expectedDepartureTime.substring(1, expectedDepartureTime.length() - 1).equals("")) {
					try {
						final Date date1 = format.parse(expectedDepartureTime.substring(1, expectedDepartureTime.length() - 1));
						if (next != null && !date1.before(calendar.getTime())) {
							if (!date1.before(next)) {
								nextapp = new Approach(approach.get(0).get("vehicleRef").toString(), approach.get(0).get("aimedArrivalTime").toString(), approach.get(0).get("aimedDepartureTime").toString(), approach.get(0).get("arrivalStatus").toString(), approach.get(0).get("destinationRef").toString(), approach.get(0).get("expectedArrivalTime").toString(), approach.get(0).get("expectedDepartureTime").toString());
							}
						} else {
							nextapp = new Approach(approach.get(0).get("vehicleRef").toString(), approach.get(0).get("aimedArrivalTime").toString(), approach.get(0).get("aimedDepartureTime").toString(), approach.get(0).get("arrivalStatus").toString(), approach.get(0).get("destinationRef").toString(), approach.get(0).get("expectedArrivalTime").toString(), approach.get(0).get("expectedDepartureTime").toString());
						}
					} catch (final ParseException e) {
						//on continue
					}
				} else if (!aimedDepartureTime.substring(1, aimedDepartureTime.length() - 1).equals("")) {
					try {
						final Date date1 = format.parse(aimedDepartureTime.substring(1, aimedDepartureTime.length() - 1));
						if (next != null && !date1.before(calendar.getTime())) {
							if (!date1.before(next)) {
								nextapp = new Approach(approach.get(0).get("vehicleRef").toString(), approach.get(0).get("aimedArrivalTime").toString(), approach.get(0).get("aimedDepartureTime").toString(), approach.get(0).get("arrivalStatus").toString(), approach.get(0).get("destinationRef").toString(), approach.get(0).get("expectedArrivalTime").toString(), approach.get(0).get("expectedDepartureTime").toString());
							}
						} else {
							nextapp = new Approach(approach.get(0).get("vehicleRef").toString(), approach.get(0).get("aimedArrivalTime").toString(), approach.get(0).get("aimedDepartureTime").toString(), approach.get(0).get("arrivalStatus").toString(), approach.get(0).get("destinationRef").toString(), approach.get(0).get("expectedArrivalTime").toString(), approach.get(0).get("expectedDepartureTime").toString());
						}
					} catch (final ParseException e) {
						//on continue
					}
				} else if (!aimedArrivalTime.substring(1, aimedArrivalTime.length() - 1).equals("")) {
					try {
						final Date date1 = format.parse(aimedArrivalTime.substring(1, aimedArrivalTime.length() - 1));
						if (next != null && !date1.before(calendar.getTime())) {
							if (!date1.before(next)) {
								nextapp = new Approach(approach.get(0).get("vehicleRef").toString(), approach.get(0).get("aimedArrivalTime").toString(), approach.get(0).get("aimedDepartureTime").toString(), approach.get(0).get("arrivalStatus").toString(), approach.get(0).get("destinationRef").toString(), approach.get(0).get("expectedArrivalTime").toString(), approach.get(0).get("expectedDepartureTime").toString());
							}
						} else {
							nextapp = new Approach(approach.get(0).get("vehicleRef").toString(), approach.get(0).get("aimedArrivalTime").toString(), approach.get(0).get("aimedDepartureTime").toString(), approach.get(0).get("arrivalStatus").toString(), approach.get(0).get("destinationRef").toString(), approach.get(0).get("expectedArrivalTime").toString(), approach.get(0).get("expectedDepartureTime").toString());
						}
					} catch (final ParseException e) {
						//on continue
					}
				}
			}
		}
		return nextapp;
	}

	@Override
	public List<Stop> getNextPassages() {
		final Driver driver = neo4JDriverProvider.getNeo4jDriver();
		try (Session session = driver.session()) {
			StatementResult rs;
			rs = session.run("Match (s:Stop) WHERE EXISTS(s.MonitoringRef) AND EXISTS(s.lat) AND EXISTS(s.lon) AND EXISTS(s.name) RETURN s ");

			return rs.list()
					.stream()
					.map(RailwayServicesImpl::buildStopApproach)
					.collect(Collectors.toList());
		}
	}

	private static Stop buildStopApproach(final Record record) {
		final Approach approach = nextApproach(record.get(0).get("id").toString());
		return new Stop(Double.parseDouble(record.get(0).get("lat").toString()), Double.parseDouble(record.get(0).get("lon").toString()), record.get(0).get("id").toString().substring(1, record.get(0).get("id").toString().length() - 1), record.get(0).get("name").toString(), Integer.parseInt(record.get(0).get("location_type").toString().replaceAll("\"", "")), approach);

	}

	@Override
	public List<Stop> getAllStops(final String CatDay, final int hour) {
		final Driver driver = neo4JDriverProvider.getNeo4jDriver();
		try (Session session = driver.session()) {
			final Map<String, Object> parameters = new HashMap<>();
			parameters.put("CatDay", CatDay);
			StatementResult rs;
			if (hour == 23) {
				parameters.put("hour", hour + "H-0H");
				rs = session.run("match (n:Stop)-->(:CatDay{type:{CatDay}})-->(h:HourSummary{Timeslot:{hour}})" +
						"WHERE n.id CONTAINS \"Point\" AND EXISTS(n.lat) AND EXISTS(n.lon) AND EXISTS(n.name) RETURN n,h");
			} else {
				parameters.put("hour", hour + "H-" + (hour + 1) + "H");
				rs = session.run("match (n:Stop)-->(:CatDay{type:{CatDay}})-->(h:HourSummary{Timeslot:{hour}})" +
						"WHERE n.id CONTAINS \"Point\" AND EXISTS(n.lat) AND EXISTS(n.lon) AND EXISTS(n.name) RETURN n,h");
			}

			return rs.list()
					.stream()
					.map(RailwayServicesImpl::buildStop)
					.collect(Collectors.toList());
		}
	}

	/*public int closestLineString(MultiLineString multi,Point point) {
		GeometryFactory factory = new GeometryFactory();
		int result=-1;
		double mindist=-1;
		for(int i=0;i<multi.getNumGeometries();i++) {
			Coordinate[] c = DistanceOp.nearestPoints(multi.getGeometryN(i), point);
			Point point1=factory.createPoint(c[0]);
			try {
				double dist = JTS.orthodromicDistance(point.getCoordinate(), point1.getCoordinate(), DefaultGeographicCRS.WGS84);
				if(mindist==-1) {
					mindist=dist;
					result=i;
				}
				else {
					if(dist<mindist) {
						mindist=dist;
						result=i;
					}
				}
			} catch (TransformException e) {
				e.printStackTrace();
			}
		}
		return result;
	}*/
	/*public Point getDistance(Point point, MultiLineString line) {
		GeometryFactory factory = new GeometryFactory();
		GeometryJSON gjson = new GeometryJSON();

		//double dist = -1.0;
		Point point1=null;
		try {
			String code = "AUTO:42001," + point.getX() + "," + point.getY();
			CoordinateReferenceSystem auto = CRS.decode(code);
			MathTransform transform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, auto);
			Geometry g3 = JTS.transform(line.getGeometryN(closestLineString(line, point)), transform);
			Geometry g4 = JTS.transform(point, transform);

			Coordinate c1 = new Coordinate();
			JTS.transform(c[0], c1, transform.inverse());

			System.out.println(point);
		System.out.println(point1);*/
	/*for(int k=1;k<18;k++) {
		Coordinate[] c = DistanceOp.nearestPoints(line.getGeometryN(closestLineString(line, point)),point);
		final double scale = Math.pow(10, k);
		final PrecisionModel pm = new PrecisionModel(scale);
		pm.makePrecise(c[0]);

		point1=factory.createPoint(c[0]);
		System.out.println(point1);
		System.out.println(line.getGeometryN(closestLineString(line, point)).disjoint(point1));
	}

	Coordinate[] c = DistanceOp.nearestPoints(line,point);
	point1=factory.createPoint(c[0]);
	System.out.println(line.intersects(point1));

	//System.out.println(DistanceOp.distance(line, point1));

	} catch (Exception e) {
	e.printStackTrace();
	}
	return point1;
	}*/
	@Override
	public void getTest() {
		/*GeometryJSON gjson = new GeometryJSON();
		GeometryFactory factory = new GeometryFactory();
		List<Stop> stops=new ArrayList<>();
		List<String> lineString=new ArrayList<>();
		Driver driver = neo4JDriverProvider.getNeo4jDriver();
		try (Session session = driver.session()) {
			StatementResult rs;
			rs = session.run("MATCH (n:Route) WHERE EXISTS(n.IDREFLIGC) AND EXISTS(n.geoshape) RETURN n");
			while(rs.hasNext()) {
				Record record=rs.next();
				try {
					String txt=record.get(0).get("geoshape").toString().replace("\\\"", "\"");
					MultiLineString multi=gjson.readMultiLine(txt.substring(1,txt.length()-1));
					StatementResult rs1;
					rs1 = session.run("MATCH (n:Stop)-->(rs:RouteSection) WHERE rs.route_id="+record.get(0).get("id").toString()+" RETURN DISTINCT n");
					while(rs1.hasNext()){
						Record record1=rs1.next();
						Coordinate coor=new Coordinate(Double.parseDouble(record1.get(0).get("lon").toString()),Double.parseDouble(record1.get(0).get("lat").toString()));
						Point point =factory.createPoint(coor);
						Point point2=getDistance(point, multi);
					}
					CoordinateArraySequence sequence=new CoordinateArraySequence(multi.getCoordinates());
					GeoJSONWriter writer1 = new GeoJSONWriter();
					GeoJSON json = writer1.write(multi);
					String jsonstring = json.toString();
					System.out.println(jsonstring);
				} catch (IOException e) {
					e.printStackTrace();
					//on continue
				}
			}
		}*/
	}

	@Override
	public List<Stop> getAllStopsinTown(final String CatDay, final int hour, final int townid) {
		final Driver driver = neo4JDriverProvider.getNeo4jDriver();
		try (Session session = driver.session()) {
			final Map<String, Object> parameters = new HashMap<>();
			parameters.put("CatDay", CatDay);
			parameters.put("townid", townid);
			StatementResult rs;
			if (hour == 23) {
				parameters.put("hour", hour + "H-0H");
				rs = session.run("match (p:Town{id:{townid}})<--(n:Stop})-->(:CatDay{type:{CatDay}})-->(h:HourSummary{Timeslot:{hour})" +
						"WHERE n.id CONTAINS \"Point\" AND EXISTS(n.lat) AND EXISTS(n.lon) AND EXISTS(n.name) RETURN n,h");
			} else {
				parameters.put("hour", hour + "H-" + (hour + 1) + "H");
				rs = session.run("match (p:Town{id:{townid}})<--(n:Stop)-->(:CatDay{type:{CatDay}})-->(h:HourSummary{Timeslot:{hour}})" +
						"WHERE n.id CONTAINS \"Point\" AND EXISTS(n.lat) AND EXISTS(n.lon) AND EXISTS(n.name) RETURN n,h");
			}

			return rs.list()
					.stream()
					.map(RailwayServicesImpl::buildStop)
					.collect(Collectors.toList());
		}

	}

	private static Stop buildStop(final Record record) {
		return new Stop(Double.parseDouble(record.get(0).get("lat").toString()), Double.parseDouble(record.get(0).get("lon").toString()), record.get(0).get("id").toString(), record.get(0).get("name").toString(), Integer.parseInt(record.get(0).get("location_type").toString().replaceAll("\"", "")),
				new HourSummary(Integer.parseInt(record.get(1).get("nb_vald").toString()), record.get(1).get("Timeslot").toString()));
	}

	@Override
	public void LdaStopMonitoringRef() {
		final String url = paramManager.getParam("opendata.stif.url").getValueAsString();
		final String rootFolder = paramManager.getParam("server.rootFolder").getValueAsString();
		try {
			Utilities.downloadfile(new URL(url + "referentiel-arret-tc-idf/download/?format=csv&timezone=Europe/Berlin&use_labels_for_header=true"), "src/main/resources/referentiel-arret-tc-idf.txt");
			Utilities.downloadfile(new URL(url + "perimetre-tr-plateforme-stif/download/?format=csv&timezone=Europe/Berlin&use_labels_for_header=true"), "src/main/resources/perimetre-tr-plateforme-stif.txt");
			Utilities.downloadfile(new URL(url + "liste-arrets-lignes-tc-idf/download/?format=csv&timezone=Europe/Berlin&use_labels_for_header=true"), "src/main/resources/liste-arrets-lignes-tc-idf.txt");
		} catch (final MalformedURLException e1) {
			throw WrappedException.wrap(e1);
		}
		final Map<Integer, Integer> map1 = new TreeMap<>(); //(ZDEr_ID_REF_A / LDA_ID_REF_A)
		final Map<Integer, String> map2 = new TreeMap<>(); //(ZDEr_ID_REF_A / stop_id)
		final Map<String, String> map3 = new TreeMap<>(); //(stop_id / MonitoringRef)
		try (BufferedReader lines = new BufferedReader(new FileReader(rootFolder + "referentiel-arret-tc-idf.txt"))) {
			String line = lines.readLine();
			while ((line = lines.readLine()) != null) {
				final String[] array1 = line.split(";");
				try {
					map1.put(Integer.parseInt(array1[0]), Integer.parseInt(array1[9]));
				} catch (final Exception e) {
					//si les conversions ne se font pas, passer à la ligne suivante
				}
			}
			lines.close();
		} catch (final IOException e2) {
			throw WrappedException.wrap(e2);
		}
		try (BufferedReader lines = new BufferedReader(new FileReader(rootFolder + "liste-arrets-lignes-tc-idf.txt"))) {
			String line = lines.readLine();
			while ((line = lines.readLine()) != null) {
				final String[] array1 = line.split(";");
				if (!array1[9].equals("")) {
					map2.put(Integer.parseInt(array1[9]), array1[5]);
				}

			}
			lines.close();
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}
		try (BufferedReader lines = new BufferedReader(new FileReader(rootFolder + "perimetre-tr-plateforme-stif.txt"))) {
			String line = lines.readLine();
			while ((line = lines.readLine()) != null) {
				final String[] array1 = line.split(";");
				map3.put(array1[7], array1[0]);
			}
			lines.close();
		} catch (final IOException e1) {
			throw WrappedException.wrap(e1);
		}
		final Set<Map.Entry<Integer, Integer>> st = map1.entrySet();
		try (PrintWriter writer = new PrintWriter(rootFolder + "LDA_Stopid.txt", "UTF-8")) {
			writer.println("stop_id;LDA_REF;MonitoringRef");
			for (final Map.Entry<Integer, Integer> me : st) {
				if (map2.get(me.getKey()) != null) {
					if (map3.get(map2.get(me.getKey())) != null) {
						writer.println(map2.get(me.getKey()) + ";" + me.getValue() + ";" + map3.get(map2.get(me.getKey())));
					} else {
						writer.println(map2.get(me.getKey()) + ";" + me.getValue());
					}
				}
			}
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			throw WrappedException.wrap(e);
		}
	}

	@Override
	public List<Route> getAllRoutes() {
		final Driver driver = neo4JDriverProvider.getNeo4jDriver();
		try (Session session = driver.session()) {
			final List<Route> result = new ArrayList<>();
			final StatementResult rs = session.run("match (r:Route) WHERE NOT (r.geoshape is null) RETURN r");
			while (rs.hasNext()) {
				final Record record = rs.next();
				result.add(new Route(record.get(0).get("id").toString(), Integer.parseInt(record.get(0).get("type").toString()), record.get(0).get("short_name").toString(), record.get(0).get("long_name").toString(), record.get(0).get("geoshape").toString()));
			}
			return result;
		}
	}

	@Override
	public void infoTraffic() {
		final String api_url = paramManager.getParam("api.stif.traffic.url").getValueAsString();
		try {
			final Map<String, String> env = new HashMap<>();
			env.put("create", "true");
			final URL url = new URL(api_url + "messages-it-stif/general-message?LineRef=ALL&apikey=38742a9af7df7bee0ad9554c8b0faf8c5604027aaa3c4fdf91a5634e");
			final String time = Instant.now().toString();
			final File f = new File("infotraffic-" + time.substring(0, time.indexOf(".")).replace(":", "-") + ".json");
			FileUtils.copyURLToFile(url, f);
			final String body = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
			final Path path = Paths.get("InfoTraffic.zip");
			final URI uri = URI.create("jar:" + path.toUri());
			try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
				final Path nf = fs.getPath(f.getName());
				try (Writer writer = Files.newBufferedWriter(nf, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
					writer.write(body);
				}
			}
			final JSONObject test = new JSONObject(body);
			final JSONArray arr = test.getJSONObject("Siri").getJSONObject("ServiceDelivery").getJSONArray("GeneralMessageDelivery");
			final JSONArray arr1 = arr.getJSONObject(0).getJSONArray("InfoMessage");
			String message = "No message";
			if (!arr1.isNull(0)) {
				for (int i = 0; i < arr1.length(); i++) {
					final String infoChannelRef = arr1.getJSONObject(i).getJSONObject("InfoChannelRef").getString("value");
					final JSONArray messages = arr1.getJSONObject(i).getJSONObject("Content").getJSONArray("Message");
					String lineid = "";
					if (!arr1.getJSONObject(i).getJSONObject("Content").isNull("LineRef")) {
						lineid = arr1.getJSONObject(i).getJSONObject("Content").getJSONArray("LineRef").getJSONObject(0).getString("value");
						lineid = lineid.substring(11, lineid.lastIndexOf(":"));
					}
					final String id = arr1.getJSONObject(i).getJSONObject("InfoMessageIdentifier").getString("value");
					final String validity = arr1.getJSONObject(i).get("ValidUntilTime").toString();
					for (int j = 0; j < messages.length(); j++) {
						final String text = messages.getJSONObject(j).getString("MessageType");
						if (text.equals("TEXT_ONLY")) {
							message = messages.getJSONObject(j).getJSONObject("MessageText").getString("value");
						}
					}
					try {
						final JSONArray points = arr1.getJSONObject(i).getJSONObject("Content").getJSONArray("StopPointRef");
						if (points.length() >= 1 && lineid != "") {
							for (int j = 0; j < points.length(); j++) {
								final String pointref = points.getJSONObject(j).getString("value");
								try (Session session = neo4JDriverProvider.getNeo4jDriver().session()) {
									session.run(" match (r:Route {IDREFLIGC:\"" + lineid + "\"}),(p:Stop{MonitoringRef:\"" + pointref + "\"})  " +
											"MERGE (r)<-[:Alert]-(c:Information{id:\"" + id + "\"})-[:Alert]->(p) SET c.type=\"" + infoChannelRef + "\",c.validity=\"" + validity + "\",c.message=\"" + message + "\";");
								}
							}

						} else if (points.length() >= 1 && lineid == "") {
							for (int j = 0; j < points.length(); j++) {
								final String pointref = points.getJSONObject(j).getString("value");
								try (Session session = neo4JDriverProvider.getNeo4jDriver().session()) {
									session.run("match (p:Stop{MonitoringRef:\"" + pointref + "\"})  " +
											"MERGE (c:Information{id:\"" + id + "\"})-[:Alert]->(p) SET c.type=\"" + infoChannelRef + "\",c.validity=\"" + validity + "\",c.message=\"" + message + "\";");
								}
							}
						} else if (lineid != "") {
							try (Session session = neo4JDriverProvider.getNeo4jDriver().session()) {
								session.run(" match (r:Route {IDREFLIGC: " + lineid + "})  " +
										"MERGE (r)<-[:Alert]-(c:Information{id:\"" + id + "\"}) SET c.type=\"" + infoChannelRef + "\",c.validity=\"" + validity + "\",c.message=\"" + message + "\";");
							}
						}
					} catch (final org.json.JSONException e) {
						try (Session session = neo4JDriverProvider.getNeo4jDriver().session()) {
							session.run(" match (r:Route {IDREFLIGC:\"" + lineid + "\"})  " +
									"MERGE (r)<-[:Alert]-(c:Information{id:\"" + id + "\"}) SET c.type=\"" + infoChannelRef + "\",c.validity=\"" + validity + "\",c.message=\"" + message + "\";");
						}
					}
				}
				f.delete();
			}

		} catch (final IOException e) {
			WrappedException.wrap(e);
		}

	}

	@Override
	public void infoPassages() {

		final String api_url = paramManager.getParam("api.stif.passages.url").getValueAsString();
		try {
			final Map<String, String> env = new HashMap<>();
			env.put("create", "true");
			final URL url = new URL(api_url + "tr-globale-stif/estimated-timetable?apikey=cefe7cc77223fb5869a18672b138bce695075840cbfc80f98b7bff37");
			final String time = Instant.now().toString();
			final File f = new File("infopassages-" + time.substring(0, time.indexOf(".")).replace(":", "-") + ".json");
			FileUtils.copyURLToFile(url, f);
			final String body = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
			final Path path = Paths.get("InfoPassages.zip");
			final URI uri = URI.create("jar:" + path.toUri());
			try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
				final Path nf = fs.getPath(f.getName());
				try (Writer writer = Files.newBufferedWriter(nf, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
					writer.write(body);
				}
			}
			final JSONObject test = new JSONObject(body);
			final JSONArray arr = test.getJSONObject("Siri").getJSONObject("ServiceDelivery").getJSONArray("EstimatedTimetableDelivery");
			final JSONArray arr1 = arr.getJSONObject(0).getJSONArray("EstimatedJourneyVersionFrame").getJSONObject(0).getJSONArray("EstimatedVehicleJourney");
			if (!arr1.isNull(0)) {
				for (int i = 0; i < arr1.length(); i++) {
					final JSONObject jsonObject1 = arr1.getJSONObject(i);
					try {
						final String vehicleRef = jsonObject1.getJSONObject("DatedVehicleJourneyRef").getString("value");
						final JSONArray points = jsonObject1.getJSONObject("EstimatedCalls").getJSONArray("EstimatedCall");
						String destinationRef = "";
						if (!jsonObject1.isNull("DestinationRef")) {
							destinationRef = jsonObject1.getJSONObject("DestinationRef").getString("value");
						}
						String lineRef = "";
						if (!jsonObject1.isNull("LineRef")) {
							lineRef = jsonObject1.getJSONObject("LineRef").getString("value");
						}
						for (int j = 0; j < points.length(); j++) {
							final JSONObject jsonObject = points.getJSONObject(j);
							final String monitoringRef = jsonObject.getJSONObject("StopPointRef").getString("value");
							String aimedArrivalTime = "";
							if (!points.getJSONObject(j).isNull("AimedArrivalTime")) {
								aimedArrivalTime = jsonObject.getString("AimedArrivalTime");
							}
							String aimedDepartureTime = "";
							if (!jsonObject.isNull("AimedDepartureTime")) {
								aimedDepartureTime = jsonObject.getString("AimedDepartureTime");
							}

							String arrivalStatus = "";
							if (!jsonObject.isNull("ArrivalStatus")) {
								arrivalStatus = jsonObject.getString("ArrivalStatus");
							}
							String expectedArrivalTime = "";
							if (!jsonObject.isNull("ExpectedArrivalTime")) {
								expectedArrivalTime = jsonObject.getString("ExpectedArrivalTime");
							}
							String expectedDepartureTime = "";
							if (!jsonObject.isNull("ExpectedDepartureTime")) {
								expectedDepartureTime = jsonObject.getString("ExpectedDepartureTime");
								try (Session session = neo4JDriverProvider.getNeo4jDriver().session()) {
									session.run("MATCH (s:Stop {MonitoringRef:\"" + monitoringRef + "\"}) "
											+ "MERGE (s)-[:Next]->(c:Approach {vehicleRef:\"" + vehicleRef + "\"}) ON MATCH SET c.destinationRef=\"" + destinationRef + "\",c.lineRef=\"" + lineRef + "\",c.arrivalStatus=\"" + arrivalStatus + "\",c.aimedArrivalTime=\"" + aimedArrivalTime + "\",c.aimedDepartureTime=\"" + aimedDepartureTime + "\",c.expectedArrivalTime=\"" + expectedArrivalTime + "\",c.expectedDepartureTime=\"" + expectedDepartureTime + "\" "
											+ "ON CREATE SET c.destinationRef=\"" + destinationRef + "\",c.lineRef=\"" + lineRef + "\",c.arrivalStatus=\"" + arrivalStatus + "\",c.expectedDepartureTime=\"" + expectedDepartureTime + "\",c.aimedArrivalTime=\"" + aimedArrivalTime + "\",c.aimedDepartureTime=\"" + aimedDepartureTime + "\",c.expectedArrivalTime=\"" + expectedArrivalTime + "\",c.update=false ;");
								}
							}

						}
					} catch (final Exception e) {
						// si la liste des calls est vide ne rien faire pour cette ligne
					}
					try (Session session = neo4JDriverProvider.getNeo4jDriver().session()) {
						session.run("MATCH (n:Stop)-->(p:Approach) WHERE NOT EXISTS(n.id) DETACH DELETE n,p;");
					}
				}
			}
			f.delete();
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}

	}

	@Override
	public void updatePassagesHistory() {
		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		final int day = calendar.get(Calendar.DAY_OF_MONTH);
		final int month = calendar.get(Calendar.MONTH);
		final int year = calendar.get(Calendar.YEAR);
		StatementResult st = null;
		try (Session session = neo4JDriverProvider.getNeo4jDriver().session()) {
			session.run("MERGE (d:Day{day:" + day + ",month:" + (month + 1) + ",year:" + year + "})");
			st = session.run("MATCH (d:Approach{update:false})<--(s:Stop) RETURN d,s;");
		} catch (final Exception e) {
			WrappedException.wrap(e);
		}
		while (st.hasNext()) {
			final Record record = st.next();
			final String ref = record.get(0).get("vehicleRef").toString();
			String arrivalStatus = record.get(0).get("arrivalStatus").toString();
			if (arrivalStatus != null) {
				arrivalStatus = arrivalStatus.substring(1, arrivalStatus.length() - 1);
				String monitoringRef = record.get(1).get("MonitoringRef").toString();
				monitoringRef = monitoringRef.substring(1, monitoringRef.length() - 1);
				try (Session session = neo4JDriverProvider.getNeo4jDriver().session()) {
					session.run("MATCH (d:Approach{vehicleRef:" + ref + "}) SET d.update=true");
					if (arrivalStatus.equals("DELAYED")) {
						session.run(" MATCH (d:Day{day:" + day + ",month:" + (month + 1) + ",year:" + year + "}),(s:Stop{MonitoringRef:\"" + monitoringRef + "\"}) MERGE (d)<-[r:DAY_SUM]-(s) ON CREATE SET r.delays = 1 " +
								"ON MATCH SET r.delays = CASE WHEN r.delays IS NULL THEN 1 ELSE r.delays+1 END");
					} else if (arrivalStatus.equals("CANCELLED")) {
						session.run(" MATCH (d:Day{day:" + day + ",month:" + (month + 1) + ",year:" + year + "}),(s:Stop{MonitoringRef:\"" + monitoringRef + "\"}) MERGE (d)<-[r:DAY_SUM]-(s) ON CREATE SET r.cancel = 1 " +
								"ON MATCH SET r.cancel = CASE WHEN r.cancel IS NULL THEN 1 ELSE r.cancel+1 END");
					} else if (arrivalStatus.equals("ON_TIME")) {
						session.run(" MATCH (d:Day{day:" + day + ",month:" + (month + 1) + ",year:" + year + "}),(s:Stop{MonitoringRef:\"" + monitoringRef + "\"}) MERGE (d)<-[r:DAY_SUM]-(s) ON CREATE SET r.ontime = 1 " +
								"ON MATCH SET r.ontime = CASE WHEN r.ontime IS NULL THEN 1 ELSE r.ontime+1 END");
					} else if (arrivalStatus.equals("EARLY")) {
						session.run(" MATCH (d:Day{day:" + day + ",month:" + (month + 1) + ",year:" + year + "}),(s:Stop{MonitoringRef:\"" + monitoringRef + "\"}) MERGE (d)<-[r:DAY_SUM]-(s) ON CREATE SET r.early = 1 " +
								"ON MATCH SET r.early = CASE WHEN r.early IS NULL THEN 1 ELSE r.early+1 END");
					}
				} catch (final Exception e) {
					WrappedException.wrap(e);
				}
			}

		}

	}

	public void allfalse() {
		try (Session session = neo4JDriverProvider.getNeo4jDriver().session()) {
			session.run("MATCH (d:Approach) SET d.update=false");
		}
	}

}
