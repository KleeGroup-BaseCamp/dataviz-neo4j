package data_Tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class Railway_Network_Converter {

	public static int calculateAverage(List<Integer> values1) {
		int sum = 0;
		if (!values1.isEmpty()) {
			for (Integer mark : values1) {
				sum += mark;
			}
			return sum / values1.size();
		}
		return sum;
	}

	public static Object getKeyFromValue(Map<Integer, Integer> hm, Object value) {
		for (Object o : hm.keySet()) {
			if (hm.get(o).equals(value)) {
				return o;
			}
		}
		return null;
	}

	public static void associate() {
		Map<Integer, Integer> map1 = new TreeMap<>(); //(ZDEr_ID_REF_A / LDA_ID_REF_A)
		Map<Integer, String> map2 = new TreeMap<>(); //(ZDEr_ID_REF_A / stop_id)
		BufferedReader lines = null;
		PrintWriter writer = null;
		String line;
		try {
			lines = new BufferedReader(new FileReader("src/data_Tools/referentiel-arret-tc-idf.txt"));
			line = lines.readLine();
			while ((line = lines.readLine()) != null) {
				String[] array1 = line.split(";");
				map1.put(Integer.parseInt(array1[0]), Integer.parseInt(array1[9]));
			}
			lines.close();
		} catch (Exception e) {

		}
		try {
			lines = new BufferedReader(new FileReader("src/data_Tools/liste-arrets-lignes-tc-idf.txt"));
			line = lines.readLine();
			while ((line = lines.readLine()) != null) {
				String[] array1 = line.split(";");
				if (!array1[9].equals("")) {
					map2.put(Integer.parseInt(array1[9]), array1[5]);
				}

			}
			lines.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Set<Map.Entry<Integer, Integer>> st = map1.entrySet();
		try {
			writer = new PrintWriter("src/data_Tools/LDA_Stopid.txt", "UTF-8");
			writer.println("stop_id;LDA_REF");
			for (Map.Entry<Integer, Integer> me : st) {
				if (map2.get(me.getKey()) != null) {
					writer.println(map2.get(me.getKey()) + ";" + me.getValue());
				}
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}

	public static void stockage() {
		Map<Integer, Integer> mapid = new TreeMap<>();

		BufferedReader rates = null;
		int current_value = 0;
		try {
			rates = new BufferedReader(new FileReader("src/data_Tools/validations-sur-le-reseau-ferre-profils-horaires-par-jour-type-1er-sem.txt"));
			String line = rates.readLine();
			while ((line = rates.readLine()) != null) {
				String[] array1 = line.split(";");
				try {
					int id = Integer.parseInt(array1[4]);
					if (!mapid.containsKey(id)) {
						mapid.put(id, current_value);
						current_value++;
					}
				} catch (Exception e) {
				}
			}
			rates.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Double[][][] values = new Double[mapid.size()][5][24];
		String rates_line = null;
		try {
			rates = new BufferedReader(new FileReader("src/data_Tools/validations-sur-le-reseau-ferre-profils-horaires-par-jour-type-1er-sem.txt"));
			while ((rates_line = rates.readLine()) != null) {
				String[] array1 = rates_line.split(";");
				int int_type = Dates.Convert_DateType_int(array1[5]);
				try {
					Integer value = mapid.get(Integer.parseInt(array1[4]));
					if (value != null) {
						String[] horr = array1[6].split("H-");
						values[value][int_type][Integer.parseInt(horr[0])] = Double.parseDouble(array1[7]);
					}
				} catch (Exception e) {
				}
			}
			rates.close();
		} catch (Exception e) {
		}

		BufferedReader origin = null;
		PrintWriter result = null;
		String origin_line = null;
		try {
			result = new PrintWriter("src/data_Tools/reseau_ferré_2017_1er1.txt", "UTF-8");
			origin = new BufferedReader(new FileReader("src/data_Tools/validations-sur-le-reseau-ferre-nombre-de-validations-par-jour-1er-sem .txt"));
			result.println("JOUR;CODE_STIF_TRNS;CODE_STIF_RES;CODE_STIF_ARRET;LIBELLE_ARRET;ID_REFA_LDA;CAT_JOUR;TRNC_HORR_60;CATEGORIE_TITRE;NB_VALD");
			origin_line = origin.readLine();
			String[] array1 = origin_line.split(";");
			while ((origin_line = origin.readLine()) != null) {
				array1 = origin_line.split(";");
				SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
				Date t = ft.parse(array1[0]);
				String type = Dates.DateType(t);
				int int_type = Dates.Convert_DateType_int(type);
				if (!array1[5].isEmpty()) {
					Integer value = mapid.get(Integer.parseInt(array1[5]));
					for (int i = 0; i < 24; i++) {
						try {
							Double pourc = values[value][int_type][i];
							String horr;
							if (i == 23) {
								horr = i + "H-0H";
							} else {
								horr = i + "H-" + (i + 1) + "H";
							}
							if (!array1[7].equals("Moins de 5")) {
								Double nb_val = (pourc / 100 * Integer.parseInt(array1[7]));
								int nb_val_done;
								if (nb_val > 0) {
									nb_val_done = nb_val.intValue() + 1;
								} else {
									nb_val_done = 0;
								}
								result.println(array1[1] + ";" + array1[2] + ";" + array1[3] + ";" + array1[4] + ";" + array1[5] + ";" + horr + ";" + type + ";" + array1[6] + ";" + nb_val_done);
							} else {
								result.println(array1[1] + ";" + array1[2] + ";" + array1[3] + ";" + array1[4] + ";" + array1[5] + ";" + horr + ";" + type + ";" + array1[6] + ";Moins de 5");
							}
						} catch (Exception e) {
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			result.close();
			origin.close();
		} catch (Exception e) {

		}
		@SuppressWarnings("unchecked")
		List<Integer>[][][] values1 = new ArrayList[mapid.size()][5][24];
		for (int i = 0; i < mapid.size(); i++) {
			for (int j = 0; j < 5; j++) {
				for (int k = 0; k < 24; k++) {
					values1[i][j][k] = new ArrayList<Integer>();
				}
			}
		}
		try {
			result = new PrintWriter("src/data_Tools/reseau_ferré_2017_1er_final.txt", "UTF-8");
			origin = new BufferedReader(new FileReader("src/data_Tools/reseau_ferré_2017_1er1.txt"));
			origin_line = origin.readLine();
			while ((origin_line = origin.readLine()) != null) {
				String[] array1 = origin_line.split(";");
				String[] horr = array1[5].split("H-");
				int int_type = Dates.Convert_DateType_int(array1[6]);
				Integer value = mapid.get(Integer.parseInt(array1[4]));
				if (array1[8].equals("Moins de 5")) {
					values1[value][int_type][Integer.parseInt(horr[0])].add(3);
				} else {
					values1[value][int_type][Integer.parseInt(horr[0])].add(Integer.parseInt(array1[8]));
				}

			}
			origin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		result.println("ID_REFA_LDA;CAT_JOUR;TRNC_HORR_60;NB_VALD_MOY");
		for (int i = 0; i < mapid.size(); i++) {
			for (int j = 0; j < 5; j++) {
				for (int k = 0; k < 24; k++) {
					int mean = calculateAverage(values1[i][j][k]);
					if (k == 23) {
						result.println(getKeyFromValue(mapid, i) + ";" + Dates.Convert_int_DateType(j) + ";" + k + "H-0H;" + mean);
					} else {
						result.println(getKeyFromValue(mapid, i) + ";" + Dates.Convert_int_DateType(j) + ";" + k + "H-" + (k + 1) + "H;" + mean);
					}
				}
			}
		}
		try {
			result.close();
		} catch (Exception e) {

		}
	}

	public static void Import_PublicTransport_Data() throws IOException {
		InputStream input = null;
		FileOutputStream writeFile = null;
		try {
			URL url = new URL("https://opendata.stif.info/explore/dataset/offre-horaires-tc-gtfs-idf/files/f24cf9dbf6f80c28b8edfdd99ea16aad/download/");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			input = connection.getInputStream();
			writeFile = new FileOutputStream("src/data_Tools/stif_gtfs.zip");
			byte[] buffer = new byte[1024];
			int read;
			while ((read = input.read(buffer)) > 0)
				writeFile.write(buffer, 0, read);
			writeFile.flush();

		} catch (IOException e) {
			System.out.println("Error while trying to download the file.");
			e.printStackTrace();
		} finally {
			try {
				writeFile.close();
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static void infoTraffic() {

		try {
			Map<String, String> env = new HashMap<>();
			env.put("create", "true");
			URL url = new URL("https://api-lab-trone-stif.opendata.stif.info/service/messages-it-stif/general-message?LineRef=ALL&apikey=a66782602e55b93cd57cffed0a2319ee60890dabdcdbe129630aed5d");
			String time = Instant.now().toString();
			File f = new File("infotraffic-" + time.substring(0, time.indexOf(".")).replace(":", "-") + ".json");
			FileUtils.copyURLToFile(url, new File("infotraffic-" + time.substring(0, time.indexOf(".")).replace(":", "-") + ".json"));
			String body = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
			Path path = Paths.get("InfoTraffic.zip");
			URI uri = URI.create("jar:" + path.toUri());
			try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
				Path nf = fs.getPath(f.getName());
				try (Writer writer = Files.newBufferedWriter(nf, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
					writer.write(body);
				}
			}
			JSONObject test = new JSONObject(body);
			JSONArray arr = (JSONArray) test.getJSONObject("Siri").getJSONObject("ServiceDelivery").getJSONArray("GeneralMessageDelivery");
			JSONArray arr1 = arr.getJSONObject(0).getJSONArray("InfoMessage");
			if (!arr1.isNull(0)) {
				for (int i = 0; i < arr1.length(); i++) {
					System.out.println(arr1.getJSONObject(i).getJSONObject("InfoMessageIdentifier").get("value"));
					System.out.println(arr1.getJSONObject(i).get("ValidUntilTime"));
					String infoChannelRef = arr1.getJSONObject(i).getJSONObject("InfoChannelRef").getString("value");
					JSONArray messages = arr1.getJSONObject(i).getJSONObject("Content").getJSONArray("Message");
					String lineid = arr1.getJSONObject(i).getJSONObject("Content").getJSONArray("LineRef").getJSONObject(0).getString("value");
					System.out.println(lineid);
					try {
						JSONArray points = arr1.getJSONObject(i).getJSONObject("Content").getJSONArray("StopPointRef");
						for (int j = 0; j < points.length(); j++) {
							System.out.println(points.getJSONObject(j).getString("value"));
						}
					} catch (Exception e) {
					}

					System.out.println(infoChannelRef);
					for (int j = 0; j < messages.length(); j++) {
						String text = messages.getJSONObject(j).getString("MessageType");
						if (text.equals("TEXT_ONLY")) {
							System.out.println(messages.getJSONObject(j).getJSONObject("MessageText").getString("value"));
						}
					}

				}
			}
			f.delete();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void infoPassages() {

		try {
			Map<String, String> env = new HashMap<>();
			env.put("create", "true");

			URL url = new URL("https://api-lab-trall-stif.opendata.stif.info/service/tr-globale-stif/estimated-timetable?apikey=cefe7cc77223fb5869a18672b138bce695075840cbfc80f98b7bff37");
			String time = Instant.now().toString();
			File f = new File("infopassages-" + time.substring(0, time.indexOf(".")).replace(":", "-") + ".json");
			FileUtils.copyURLToFile(url, f);
			String body = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
			Path path = Paths.get("InfoPassages.zip");
			URI uri = URI.create("jar:" + path.toUri());
			try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
				Path nf = fs.getPath(f.getAbsolutePath());
				try (Writer writer = Files.newBufferedWriter(nf, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
					writer.write(body);
				}
			}
			JSONObject test = new JSONObject(body);
			JSONArray arr = (JSONArray) test.getJSONObject("Siri").getJSONObject("ServiceDelivery").getJSONArray("EstimatedTimetableDelivery");
			JSONArray arr1 = arr.getJSONObject(0).getJSONArray("EstimatedJourneyVersionFrame").getJSONObject(0).getJSONArray("EstimatedVehicleJourney");
			if (!arr1.isNull(0)) {
				for (int i = 0; i < arr1.length(); i++) {
					String lineid = arr1.getJSONObject(i).getJSONObject("LineRef").getString("value");
					System.out.println(lineid);
					try {
						JSONArray points = arr1.getJSONObject(i).getJSONObject("EstimatedCalls").getJSONArray("EstimatedCall");
						for (int j = 0; j < points.length(); j++) {
							System.out.println(points.getJSONObject(j).getJSONObject("StopPointRef").getString("value"));
							System.out.println("ExpectedArrivalTime : " + points.getJSONObject(j).getString("ExpectedArrivalTime"));
							System.out.println("ExpectedDepartureTime : " + points.getJSONObject(j).getString("ExpectedDepartureTime"));
							System.out.println("AimedArrivalTime : " + points.getJSONObject(j).getString("AimedArrivalTime"));
							System.out.println("AimedDepartureTime : " + points.getJSONObject(j).getString("AimedDepartureTime"));
							System.out.println("ArrivalStatus : " + points.getJSONObject(j).getString("ArrivalStatus"));
						}
					} catch (Exception e) {
					}

				}
			}
			f.delete();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
