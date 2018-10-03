package com.kleegroup.stages.datavizneo4j.services;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;

import org.geotools.geojson.geom.GeometryJSON;
import org.wololo.geojson.GeoJSON;
import org.wololo.jts2geojson.GeoJSONWriter;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

import io.vertigo.core.param.ParamManager;
import io.vertigo.lang.WrappedException;

public class RouteServicesImpl implements RouteServices {

	@Inject
	private ParamManager paramManager;

	@Override
	public void MultiLinesCreation() {
		final String url = paramManager.getParam("opendata.stif.url").getValueAsString();
		final String rootFolder = paramManager.getParam("server.rootFolder").getValueAsString();
		try (BufferedInputStream in = new BufferedInputStream(new URL(url + "traces-du-reseau-ferre-idf/download/?format=csv&timezone=Europe/Berlin&use_labels_for_header=true").openStream())) {
			FileOutputStream fout = new FileOutputStream(rootFolder + "traces-du-reseau-ferre-idf.txt");
			byte data[] = new byte[1024];
			int count;
			while ((count = in.read(data, 0, 1024)) != -1) {
				fout.write(data, 0, count);
			}
			fout.close();
		} catch (IOException e) {
			throw WrappedException.wrap(e);
		}
		GeometryFactory factory = new GeometryFactory();
		GeometryJSON gjson = new GeometryJSON();
		Map<String, List<LineString>> map1 = new TreeMap<>();
		Map<String, String> map2 = new TreeMap<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(rootFolder + "traces-du-reseau-ferre-idf.txt"))) {
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] array1 = line.split(";");
				array1[1] = array1[1].replaceAll("\"\"", "\"");
				array1[1] = array1[1].substring(1, array1[1].length());
				LineString lineString = gjson.readLine(array1[1]);
				String extCode = array1[17];
				String IDREFLIGC = array1[4];
				if (!map2.containsKey(extCode)) {
					map2.put(extCode, IDREFLIGC);
				}
				if (map1.containsKey(extCode)) {
					map1.get(extCode).add(lineString);
				} else {
					map1.put(extCode, new ArrayList<>());
					map1.get(extCode).add(lineString);
				}
			}
		} catch (IOException e) {
			throw WrappedException.wrap(e);
		}
		try (PrintWriter writer = new PrintWriter(rootFolder + "MultiLineStrings.txt", "UTF-8")) {
			writer.println("EXTCODE;Geo Shape;IDREFLIGC");
			Set<Entry<String, List<LineString>>> st = map1.entrySet();
			for (Entry<String, List<LineString>> me : st) {
				LineString[] array = me.getValue().toArray(new LineString[me.getValue().size()]);
				MultiLineString multiLineString = factory.createMultiLineString(array);
				GeoJSONWriter writer1 = new GeoJSONWriter();
				GeoJSON json = writer1.write(multiLineString);
				String jsonstring = json.toString();
				String id = map2.get(me.getKey());
				writer.println(me.getKey() + ";" + jsonstring + ";" + id);
			}
		} catch (IOException e) {
			throw WrappedException.wrap(e);
		}
	}

}
