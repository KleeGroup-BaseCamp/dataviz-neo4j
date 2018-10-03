/*
 * Nom de classe : Town_Data_Gathering
 *
 * Description   : Regroupe les fonctions permettant de lire et de convertir les 
 * 					données liées aux communes en Ile-de-France.
 *
 * Version       : 1.0
 *
 * Date          : 23/04/2018
 * 
 * Copyright     : 
 */
package data_Tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;

import javax.swing.plaf.multi.MultiButtonUI;

import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.distance.DistanceOp;

public class Town_Data_Gathering {
	
	public double getDistance(Point point, LineString line) {
		double dist = -1.0;
		try {
		  String code = "AUTO:42001," + point.getX() + "," + point.getY();
		  CoordinateReferenceSystem auto = CRS.decode(code);
		  // auto = CRS.decode("epsg:2470");
		  MathTransform transform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, auto);
		  Geometry g3 = JTS.transform(line, transform);
		  Geometry g4 = JTS.transform(point, transform);

		  Coordinate[] c = DistanceOp.nearestPoints(g4, g3);

		  Coordinate c1 = new Coordinate();
		  //System.out.println(c[1].distance(g4.getCoordinate()));
		  JTS.transform(c[1], c1, transform.inverse());
		  //System.out.println(geometryFactory.createPoint(c1));
		  dist = JTS.orthodromicDistance(point.getCoordinate(), c1, DefaultGeographicCRS.WGS84);
		} catch (Exception e) {
		  e.printStackTrace();
		}
		return dist;
	}
	/**
	 * Permet d'associer une portion de route avec la commune dans laquelle elle se trouve.
	 * Un fichier est créé avec le code insee de la commune et l'id de la section de route.
	 */
	public static void road_town() {
		GeometryJSON gjson = new GeometryJSON();
		BufferedReader towns;
		BufferedReader road;
		PrintWriter writer = null;
		String currentroad;
		String currenttown;
		try {
			writer = new PrintWriter("src/data_Tools/road_town.txt", "UTF-8");
			writer.println("road,insee");
			road = new BufferedReader(new FileReader("src/data_Tools/referentiel-comptages-routiers.txt"));
			currentroad = road.readLine();
			while ((currentroad = road.readLine()) != null) {
				try {
					towns = new BufferedReader(new FileReader("src/data_Tools/les-communes-generalisees-dile-de-france.txt"));
					currenttown = towns.readLine();
					currenttown = towns.readLine();
					String[] array2 = currenttown.split(";");
					array2[4] = array2[4].replaceAll("\"\"", "\"");
					array2[4] = array2[4].substring(1, array2[4].length());
					String[] array1 = currentroad.split(";");
					array1[4] = array1[4].replaceAll("\"\"", "\"");
					array1[4] = array1[4].substring(1, array1[4].length());
					LineString line = gjson.readLine(array1[4]);
					Polygon polygon = gjson.readPolygon(array2[4]);
					if (!polygon.intersects(line)) {
						while (!polygon.intersects(line) && (currenttown = towns.readLine()) != null) {
							try {
								array2 = currenttown.split(";");
								array2[4] = array2[4].replaceAll("\"\"", "\"");
								array2[4] = array2[4].substring(1, array2[4].length());
								polygon = gjson.readPolygon(array2[4]);
							} catch (Exception e1) {
							}
						}
					}
					writer.println(array1[2] + "," + array2[2]);
				} catch (Exception e) {
				}
			}
		} catch (Exception e2) {
		}
	}

	/**
	 * Permet d'associer un arrêt de transport en commun avec la commune dans laquelle il se trouve.
	 * Un fichier est créé avec le code insee de la commune et l'id gtfs de l'arrêt.
	 */
	public static void stop_town() {
		GeometryJSON gjson = new GeometryJSON();
		BufferedReader towns;
		BufferedReader stop;
		PrintWriter writer = null;
		String currentstop;
		String currenttown;
		try {
			writer = new PrintWriter("src/data_Tools/stop_town.txt", "UTF-8");
			writer.println("stop_id,insee");
			stop = new BufferedReader(new FileReader("src/data_Tools/stops.txt"));
			currentstop = stop.readLine();
			while ((currentstop = stop.readLine()) != null) {
				try {
					towns = new BufferedReader(new FileReader("src/data_Tools/les-communes-generalisees-dile-de-france.txt"));
					currenttown = towns.readLine();
					currenttown = towns.readLine();
					String[] array2 = currenttown.split(";");
					array2[4] = array2[4].replaceAll("\"\"", "\"");
					array2[4] = array2[4].substring(1, array2[4].length());
					String[] array1 = currentstop.split(",");
					String json = "{\"type\":\"Point\",\"coordinates\":[" + array1[4] + "," + array1[3] + "]}";
					Reader reader = new StringReader(json);
					Point p = gjson.readPoint(reader);
					Polygon polygon = gjson.readPolygon(array2[4]);
					if (!polygon.contains(p)) {
						while (!polygon.contains(p) && (currenttown = towns.readLine()) != null) {
							try {
								array2 = currenttown.split(";");
								array2[4] = array2[4].replaceAll("\"\"", "\"");
								array2[4] = array2[4].substring(1, array2[4].length());
								polygon = gjson.readPolygon(array2[4]);
							} catch (Exception e1) {
							}
						}
					}
					writer.println(array1[0] + "," + array2[2]);

				} catch (Exception e) {
				}
			}
		} catch (Exception e2) {
		}
	}
}
