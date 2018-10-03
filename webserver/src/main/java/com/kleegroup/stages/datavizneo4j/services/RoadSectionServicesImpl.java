package com.kleegroup.stages.datavizneo4j.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.inject.Inject;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.geotools.geojson.geom.GeometryJSON;
import org.javatuples.Pair;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import com.kleegroup.stages.datavizneo4j.domain.DateType;
import com.kleegroup.stages.datavizneo4j.domain.Dates;
import com.kleegroup.stages.datavizneo4j.domain.HourSummary;
import com.kleegroup.stages.datavizneo4j.domain.RoadSection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import io.vertigo.core.param.ParamManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

public class RoadSectionServicesImpl implements RoadSectionServices {

	@Inject
	private Neo4JDriverProvider neo4JDriverProvider;
	@Inject
	private ParamManager paramManager;

	@Override
	public List<RoadSection> getAll(final String CatDay, final int hour) {

		final Driver driver = neo4JDriverProvider.getNeo4jDriver();
		try (Session session = driver.session()) {
			StatementResult rs;
			final Map<String, Object> parameters = new HashMap<>();
			parameters.put("CatDay", CatDay);

			if (hour == 23) {
				parameters.put("hour", hour + "H-0H");
				rs = session.run("match (n:RoadSection)-->(:CatDay{type:{CatDay}})-->(h:HourSummary{Timeslot:{hour}}) "
						+ "WHERE EXISTS(n.geoshape) AND EXISTS(n.geopoint) AND EXISTS(n.id_arc_tra) RETURN n,h", parameters);
			} else {
				parameters.put("hour", hour + "H-" + (hour + 1) + "H");
				rs = session.run("match (n:RoadSection)-->(:CatDay{type:{CatDay}})-->(h:HourSummary{Timeslot:{hour}}) "
						+ "WHERE EXISTS(n.geoshape) AND EXISTS(n.geopoint) AND EXISTS(n.id_arc_tra) RETURN n,h", parameters);
			}
			return rs.list()
					.stream()
					.map(RoadSectionServicesImpl::buildRoadSection)
					.collect(Collectors.toList());
		}
	}

	@Override
	public List<RoadSection> getRoadsInTown(final String CatDay, final int hour, final int townid) {

		final Driver driver = neo4JDriverProvider.getNeo4jDriver();
		try (Session session = driver.session()) {
			StatementResult rs;
			final Map<String, Object> parameters = new HashMap<>();
			parameters.put("CatDay", CatDay);
			parameters.put("townid", townid);
			if (hour == 23) {
				parameters.put("hour", hour + "H-0H");
				rs = session.run("match (:Town{id:{townid}})<--(n:RoadSection)-->(:CatDay{type:{CatDay})-->(h:HourSummary{Timeslot:{hour}}) "
						+ "WHERE EXISTS(n.geoshape) AND EXISTS(n.geopoint) AND EXISTS(n.id_arc_tra) RETURN n,h", parameters);
			} else {
				parameters.put("hour", hour + "H-" + (hour + 1) + "H");
				rs = session.run("match (:Town{id:{townid}})<--(n:RoadSection)-->(:CatDay{type:{CatDay}})-->(h:HourSummary{Timeslot:{hour}}) "
						+ "WHERE EXISTS(n.geoshape) AND EXISTS(n.geopoint) AND EXISTS(n.id_arc_tra) RETURN n,h", parameters);
			}
			return rs.list()
					.stream()
					.map(RoadSectionServicesImpl::buildRoadSection)
					.collect(Collectors.toList());
		}
	}

	private static RoadSection buildRoadSection(final Record record) {
		return new RoadSection(Integer.parseInt(record.get(0).get("id").toString()), Integer.parseInt(record.get(0).get("id_arc_tra").toString()),
				record.get(0).get("geoshape").toString(), record.get(0).get("geopoint").toString(), new HourSummary(Double.parseDouble(record.get(1).get("speed").toString()),
						Double.parseDouble(record.get(1).get("flow_rate").toString()), Double.parseDouble(record.get(1).get("occupancy_rate").toString()), record.get(1).get("Timeslot").toString()));
	}

	@Override
	public void DownloadingDatasets(final int year) {
		String fileName = null;
		final String zipFileName;
		final String rootFolder = paramManager.getParam("server.rootFolder").getValueAsString();
		try {
			final String baseURL = paramManager.getParam("opendata.paris.url").getValueAsString();
			final URL url = new URL(baseURL + "/comptages-routiers-permanents/attachments/" + year + "_paris_donnees_trafic_capteurs_zip");
			final URLConnection connection = url.openConnection();
			try (InputStream input = connection.getInputStream()) {
				fileName = url.getFile().substring(url.getFile().lastIndexOf('/') + 1);
				final int start = fileName.lastIndexOf("_");
				zipFileName = new StringBuilder()
						.append(fileName.substring(0, start))
						.append(".zip")
						.toString();
				try (FileOutputStream writeFile = new FileOutputStream(rootFolder + zipFileName)) {
					final byte[] buffer = new byte[1024];
					int read;
					while ((read = input.read(buffer)) > 0) {
						writeFile.write(buffer, 0, read);
					}
					writeFile.flush();
				}
			}
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}

		try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(rootFolder + zipFileName)))) {
			ZipEntry ze = null;
			while ((ze = zis.getNextEntry()) != null) {
				final File f = new File(rootFolder + year + "_paris_donnees_trafic_capteurs", ze.getName());
				if (ze.isDirectory()) {
					f.mkdirs();
					continue;
				}
				f.getParentFile().mkdirs();
				try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(f))) {
					final byte[] buf = new byte[8192];
					int bytesRead;
					while (-1 != (bytesRead = zis.read(buf))) {
						fos.write(buf, 0, bytesRead);
					}
				} catch (final IOException ioe) {
					f.delete();
					throw WrappedException.wrap(ioe);
				}
			}
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		} finally {
			final File file = new File(rootFolder + zipFileName);
			file.delete();
		}
	}

	@Override
	public void firstRegression(final int year) {
		final String rootFolder = paramManager.getParam("server.rootFolder").getValueAsString();
		final int road_max = nbRoadSection(year);
		try (final PrintWriter writer = new PrintWriter(rootFolder + "trafic_capteurs_" + year + "_regression.txt", "UTF-8")) {

			final Pair<List<Double>, List<Double>>[][] matrice = new Pair[5][road_max]; //matrice des listes de taux pour chaque type de jour (JOHV,JOVS,SAHS,SAVS,DIJFP)

			for (int i = 0; i < 5; i++) {
				for (int k = 0; k < road_max; k++) {
					matrice[i][k] = new Pair<>(new ArrayList<>(), new ArrayList<>());
				}
			}
			for (int month = 1; month < 7; month++) { //taux par semestre
				String test = null;
				int nb_roads_month = 0;
				try (final BufferedReader reader = new BufferedReader(new FileReader(rootFolder + year + "_paris_donnees_trafic_capteurs/donnees_trafic_capteurs_" + year + "0" + month + ".txt"))) {
					test = reader.readLine();
					nb_roads_month = nbRoadSectionMonth(year, month);

					final Calendar calendar = Calendar.getInstance();
					final SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

					for (int id = 1; id < nb_roads_month - 3; id++) {
						if (test != null) {
							String[] array1 = test.split("	");
							Date t = ft.parse(array1[1]);
							calendar.setTime(t);
							final int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
							for (int i = 0; i < daysInMonth; i++) {
								t = ft.parse(array1[1]);
								final DateType type = Dates.DateType(t);
								final int line = Dates.Convert_DateType_int(type);
								final int id1 = Integer.parseInt(array1[0]);
								for (int j = 0; j < 24; j++) {
									if (test == null) {
										break;
									}
									array1 = test.split("	");
									t = ft.parse(array1[1]);
									if (array1[2].length() != 0 && array1[3].length() != 0) {
										array1[2] = array1[2].replace(',', '.');
										array1[3] = array1[3].replace(',', '.');
										matrice[line][id1 - 1].getValue0().add(Double.parseDouble(array1[3]));
										matrice[line][id1 - 1].getValue1().add(Double.parseDouble(array1[2]));
									}

								}
							}
						}
					}
				} catch (final IOException | ParseException e) {
					throw WrappedException.wrap(e);
				}
			}
			for (int k = 0; k < road_max; k++) {
				for (int i = 0; i < 5; i++) {
					if (matrice[i][k].getValue1().size() > 20) {
						final List<Integer> under_index = new ArrayList<>();
						final List<Integer> above_index = new ArrayList<>();
						final Double critical = Collections.max(matrice[i][k].getValue1());
						final Double critical_to = matrice[i][k].getValue0().get(matrice[i][k].getValue1().indexOf(critical));
						int counter = 0;
						for (final Double d : matrice[i][k].getValue0()) {
							if (d > critical_to) {
								above_index.add(counter);
							} else if (d.equals(critical_to)) {
								above_index.add(counter);
								under_index.add(counter);
							} else {
								under_index.add(counter);
							}
							counter++;
						}
						final WeightedObservedPoints obs = new WeightedObservedPoints();
						obs.add(1000, 0, 0);
						PolynomialCurveFitter fitter = PolynomialCurveFitter.create(3);
						for (int y = 0; y < matrice[i][k].getValue1().size(); y++) {
							obs.add(matrice[i][k].getValue0().get(y), matrice[i][k].getValue1().get(y));
						}
						final double[] coeff = fitter.fit(obs.toList());
						obs.clear();
						double[] coeff_above = null;
						if (above_index.size() > 20) {
							fitter = PolynomialCurveFitter.create(2);
							for (final Integer index : above_index) {
								obs.add(matrice[i][k].getValue0().get(index), matrice[i][k].getValue1().get(index));
							}
							coeff_above = fitter.fit(obs.toList());
						} else {
							fitter = PolynomialCurveFitter.create(2);
							for (int y = 0; y < matrice[i][k].getValue0().size(); y++) {
								obs.add(matrice[i][k].getValue0().get(y), matrice[i][k].getValue1().get(y));
							}
							coeff_above = fitter.fit(obs.toList());
						}
						final DateType type = Dates.Convert_int_DateType(i);
						if (above_index.size() > 10) {
							writer.println((k + 1) + "," + type + "," + critical_to + "," + coeff[0] + ";" + coeff[1] + ";" + coeff[2] + ";" + coeff[3] + "," + coeff_above[0] + ";" + coeff_above[1] + ";" + coeff_above[2]);
						} else {
							writer.println((k + 1) + "," + type + "," + critical_to + "," + coeff[0] + ";" + coeff[1] + ";" + coeff[2] + ";" + coeff[3]);
						}

					}
				}
			}
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}
	}

	@Override
	public void completion(final int year) {
		final String rootFolder = paramManager.getParam("server.rootFolder").getValueAsString();
		for (int month = 1; month < 7; month++) { //taux par semestre
			String test = null;
			int nb_roads_month = 0;
			try (PrintWriter writer = new PrintWriter(rootFolder + "trafic_capteurs_first_completion_" + year + "0" + month + ".txt", "UTF-8")) {
				try (BufferedReader reader = new BufferedReader(new FileReader(rootFolder + year + "_paris_donnees_trafic_capteurs/donnees_trafic_capteurs_" + year + "0" + month + ".txt"))) {
					test = reader.readLine();
					nb_roads_month = nbRoadSectionMonth(year, month);

					final Calendar calendar = Calendar.getInstance();
					final SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

					for (int id = 1; id < nb_roads_month - 3; id++) {
						String[] array1 = test.split("	");
						Date t = ft.parse(array1[1]);
						calendar.setTime(t);
						final int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
						for (int i = 0; i < daysInMonth; i++) {
							final int id1 = Integer.parseInt(array1[0]);
							for (int j = 0; j < 24; j++) {
								if (test == null) {
									break;
								}
								array1 = test.split("	");
								t = ft.parse(array1[1]);
								final DateType type = Dates.DateType(t);
								try {
									if (!(array1[2].length() > 0 && Double.parseDouble(array1[2]) > 0)) {
										//debit inconnu
										array1[3] = array1[3].replace(',', '.');
										if (Double.parseDouble(array1[3]) > 0.0) {
											final List<Double> result = SearchRegression(id1, type);
											final Double max = result.get(0);
											final Double x = Double.parseDouble(array1[3]);
											if (x <= max) {
												final int debit = (int) (Math.round((result.get(1) + x * result.get(2) + Math.pow(x, 2) * result.get(3) + Math.pow(x, 3) * result.get(4)) * 100.0) / 100.0);
												array1[2] = debit + "";
												final String line = String.join("	", array1);
												if (debit > 0) {
													writer.println(line);
												}
											} else {
												if (result.size() >= 5) {
													int debit = (int) (Math.round((result.get(5) + x * result.get(6) + Math.pow(x, 2) * result.get(7)) * 100.0) / 100.0);
													if (debit < 0) {
														debit = 0;
													}
													array1[2] = debit + "";
													final String line = String.join("	", array1);
													if (debit >= 0) {
														writer.println(line);
													}
												}
											}
										}
									}
								} catch (final Exception e) {
									// si erreur sur la ligne on continue
								}
							}
						}
					}
				}
			} catch (final IOException | ParseException e) {
				throw WrappedException.wrap(e);
			}

		}
		for (int month = 1; month < 7; month++) {
			merging(year, month);
		}
	}

	/**
	 * Permet de trouver la ligne correspondante aux paramètres recherchés dans le fichier concernant
	 * les données sur les régréssions linéaires et de renvoyer la liste des éléments des régréssions
	 * calculées.
	 *
	 * @param 	id		Id de la section de route recherchée
	 * @param 	type	Catégorie de jour recherchée
	 * @return	Une liste de Double représentant le taux d'occupation critique, les coeffecients de l'équation de regression
	 *          polynomiale de troisième ordre qui convient pour les données situées en dessous du taux critique et enfin
	 *          les coefficients de l'équation de regression polynomiale de second ordre qui convient pour les données
	 *          situées au dessous du taux critique.
	 *
	 */
	public List<Double> SearchRegression(final int id, final DateType type) {
		String test;
		final String rootFolder = paramManager.getParam("server.rootFolder").getValueAsString();
		final List<Double> result = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(rootFolder + "trafic_capteurs_2017_regression.txt"))) {
			while ((test = reader.readLine()) != null) {
				final String[] array1 = test.split(",");
				if (array1[4] != null) {
					if (Integer.parseInt(array1[0]) == id && array1[1].equals(type.toString())) {
						result.add(Double.parseDouble(array1[2]));
						final String[] array2 = array1[3].split(";");
						for (final String temp : array2) {
							result.add(Double.parseDouble(temp));
						}
						reader.close();
						return result;
					}
				}
				if (Integer.parseInt(array1[0]) == id && array1[1].equals(type.toString())) {
					result.add(Double.parseDouble(array1[2]));
					final String[] array3 = array1[3].split(";");
					for (final String temp : array3) {
						result.add(Double.parseDouble(temp));
					}
					final String[] array4 = array1[4].split(";");
					for (final String temp : array4) {
						result.add(Double.parseDouble(temp));
					}
					reader.close();
					return result;
				}
				if (Integer.parseInt(array1[0]) > id) {
					break;
				}
			}
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}
		return null;
	}

	/**
	 * Lis, pour chaque mois, chaque ligne des fichiers de données traffics brutes et les lignes des données complétées. Si la date
	 * et l'id correspond alors il écrit la ligne du fichier des données complétées, sinon il écrit la ligne de l'ancien fichier.
	 *
	 * @param month un entier qui désigne le mois à fusionner
	 */
	public void merging(final int year, final int month) {
		final String rootFolder = paramManager.getParam("server.rootFolder").getValueAsString();
		try (PrintWriter writer = new PrintWriter(rootFolder + "trafic_capteurs_fulldata_" + year + "0" + month + ".txt", "UTF-8")) {
			try (BufferedReader reader_origin = new BufferedReader(new FileReader(rootFolder + year + "_paris_donnees_trafic_capteurs/donnees_trafic_capteurs_" + year + "0" + month + ".txt"))) {
				try (BufferedReader reader_correction = new BufferedReader(new FileReader(rootFolder + "trafic_capteurs_first_completion_" + year + "0" + month + ".txt"))) {
					String origin = null;
					String correction = null;
					correction = reader_correction.readLine();
					String[] array2 = correction.split("	");
					while ((origin = reader_origin.readLine()) != null) {
						final String[] array1 = origin.split("	");
						if (array1[0].equals(array2[0]) && array1[1].equals(array2[1])) {
							writer.println(correction);
							if ((correction = reader_correction.readLine()) != null) {
								array2 = correction.split("	");
							}
						} else {
							writer.println(origin);
						}
					}
				}
			}
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}
	}

	@Override
	public double calculateSpeed(final Double rate, final Double rateflow, final Double distance) {
		Assertion.checkNotNull(rate);
		Assertion.checkNotNull(rateflow);
		Assertion.checkNotNull(distance);
		//---
		return Math.round((distance * rateflow / (rate * 100) * 3.6) * 100.0) / 100.0;
	}

	@Override
	public int nbRoadSection(final int year) {
		final String rootFolder = paramManager.getParam("server.rootFolder").getValueAsString();
		int max = 0;
		for (int month = 1; month < 7; month++) {
			String test = null;
			try (BufferedReader reader = new BufferedReader(new FileReader(rootFolder + year + "_paris_donnees_trafic_capteurs/donnees_trafic_capteurs_" + year + "0" + month + ".txt"))) {
				test = reader.readLine();
				String[] array1 = test.split("	");
				while ((test = reader.readLine()) != null) {
					array1 = test.split("	");
					if (array1[0] != null) {
						max = Integer.parseInt(array1[0]);
					}
				}
			} catch (final IOException e) {
				throw WrappedException.wrap(e);
			}
		}
		return max;
	}

	@Override
	public int nbRoadSectionMonth(final int year, final int month) {
		final String rootFolder = paramManager.getParam("server.rootFolder").getValueAsString();
		int currentnumber = 0;
		int currentid = 0;
		try (BufferedReader reader = new BufferedReader(new FileReader(rootFolder + year + "_paris_donnees_trafic_capteurs/donnees_trafic_capteurs_" + year + "0" + month + ".txt"))) {
			String test = reader.readLine();
			String[] array1 = test.split("	");
			currentid = Integer.parseInt(array1[0]);
			while ((test = reader.readLine()) != null) {
				array1 = test.split("	");
				if (Integer.parseInt(array1[0].toString()) != currentid) {
					currentnumber++;
					currentid = Integer.parseInt(array1[0]);
				}
			}
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}
		return currentnumber;
	}

	@Override
	public void roadSectionDataConverter(final int year) {
		final String rootFolder = paramManager.getParam("server.rootFolder").getValueAsString();
		final int road_max = nbRoadSection(year);
		try (final PrintWriter writer = new PrintWriter(rootFolder + "trafic_capteurs_" + year + "_1er_semestre.txt", "UTF-8")) {
			writer.println("id,type,TRNC_HORR,rate,rateflow,speed");

			final Pair<List<Double>, List<Double>>[][][] matrice = new Pair[6][24][road_max]; //matrice des listes de taux pour chaque type de jour (JOHV,JOVS,SAHS,SAVS,DIJFP)

			for (int i = 0; i < 6; i++) {
				for (int j = 0; j < 24; j++) {
					for (int k = 0; k < road_max; k++) {
						matrice[i][j][k] = new Pair<>(new ArrayList<>(), new ArrayList<>());
					}
				}
			}
			for (int month = 1; month < 7; month++) { //taux par semestre
				String test = null;
				int nb_roads_month = 0;
				try (BufferedReader reader = new BufferedReader(new FileReader(rootFolder + "trafic_capteurs_fulldata_" + year + "0" + month + ".txt"))) {
					test = reader.readLine();
					nb_roads_month = nbRoadSectionMonth(year, month);

					final Calendar calendar = Calendar.getInstance();
					final SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

					for (int id = 1; id < nb_roads_month - 3; id++) {//  les id ne sont pas dans le bon ordre et ils manquents des données dans le fichier
						if (test != null) {
							String[] array1 = test.split("	");
							Date t = ft.parse(array1[1]);
							calendar.setTime(t);
							final int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
							for (int i = 0; i < daysInMonth; i++) {
								t = ft.parse(array1[1]);
								final DateType type = Dates.DateType(t);
								final int line = Dates.Convert_DateType_int(type);
								final int id1 = Integer.parseInt(array1[0]);
								for (int j = 0; j < 24; j++) {
									array1 = test.split("	");
									t = ft.parse(array1[1]);
									if (!array1[2].isEmpty() && !array1[3].isEmpty()) {
										array1[2] = array1[2].replace(',', '.');
										array1[3] = array1[3].replace(',', '.');
										matrice[line][j][id1 - 1].getValue0().add(Double.parseDouble(array1[3]));
										matrice[line][j][id1 - 1].getValue1().add(Double.parseDouble(array1[2]));

									}
								}
							}
						}
					}

				} catch (NumberFormatException | ParseException | IOException e) {
					throw WrappedException.wrap(e);
				}
				for (int k = 0; k < road_max; k++) {
					for (int i = 0; i < 6; i++) {
						for (int j = 0; j < 24; j++) {
							if (!matrice[i][j][k].getValue1().isEmpty() && !matrice[i][j][k].getValue0().isEmpty()) {
								final DateType type = Dates.Convert_int_DateType(i);
								final Double average_rate = Utilities.CalculateAverageDouble(matrice[i][j][k].getValue0());
								final Double average_rateflow = Utilities.CalculateAverageDouble(matrice[i][j][k].getValue1());
								final Double average_speed = calculateSpeed(average_rate, average_rateflow, 4.5);
								if (j == 23) {
									writer.println((k + 1) + "," + type + ",23H-0H," + average_rate + "," + average_rateflow + "," + average_speed);
								} else {
									writer.println((k + 1) + "," + type + "," + j + "H-" + (j + 1) + "H," + average_rate + "," + average_rateflow + "," + average_speed);
								}
							}
						}
					}
				}
			}
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			throw WrappedException.wrap(e);
		}
	}

	@Override
	public void LimitationAreaDataConverter() throws IOException {
		// Téléchargement du fichier csv
		try (BufferedInputStream in = new BufferedInputStream(new URL("https://opendata.paris.fr/explore/dataset/zones-de-circulation-apaisee-a-paris/download/?format=csv&timezone=Europe/Berlin&use_labels_for_header=true").openStream())) {
			final FileOutputStream fout = new FileOutputStream("src/main/resources/zones_30_paris.txt");
			final byte data[] = new byte[1024];
			int count;
			while ((count = in.read(data, 0, 1024)) != -1) {
				fout.write(data, 0, count);
			}
			fout.close();
		}

		// Enregistrement des zones 30 et 20 dans une map
		try (BufferedReader in = new BufferedReader(new FileReader("src/main/resources/zones_30_paris.txt"))) {

			String line = in.readLine();

			try (final PrintWriter fout = new PrintWriter("src/main/resources/limitation-areas.txt", "UTF-8")) {

				fout.println("area_id;polygon_shape;speed_limitation");
				while ((line = in.readLine()) != null) {
					final String[] array1 = line.split(";");
					//	array1[9] = array1[9].replaceAll("\"\"", "\"");
					//	array1[9] = array1[9].substring(1, array1[9].length());
					//	array1[9] = gjson.readPolygon(array1[9]) ;
					try {

						if (array1[8].equals("ZONE 30")) {
							array1[8] = "30";
						} else if (array1[8].equals("")) {
							array1[8] = "20";
						}
						fout.println(array1[0] + ";" + array1[9] + ";" + array1[8]);
					} catch (final Exception e) {
						// nothing
					}
				}
				fout.close();
			}
			in.close();
		} catch (final Exception e) {
			// nothing
		}
	}

	@Override
	public void connectRoadtoLimitationAreas() throws IOException {
		final GeometryJSON gjson = new GeometryJSON();

		// Lecture de toutes les routes et ajout de leur id associé à la vitesse de limitation s'il y a lieu
		try (final PrintWriter writer = new PrintWriter("src/main/resources/road_speed.txt", "UTF-8")) {

			writer.println("ID_ROAD;ID_AREA");

			final BufferedReader roads = new BufferedReader(new FileReader("src/main/resources/referentiel-comptages-routiers.txt"));
			String currentroad = roads.readLine();

			while ((currentroad = roads.readLine()) != null) {

				try {
					final String[] array_paris = currentroad.split(";");
					array_paris[4] = array_paris[4].replaceAll("\"\"", "\"");
					array_paris[4] = array_paris[4].substring(1, array_paris[4].length());
					final LineString line = gjson.readLine(array_paris[4]);

					final BufferedReader areas = new BufferedReader(new FileReader("src/main/resources/limitation-areas.txt"));
					String currentarea = areas.readLine();

					while ((currentarea = areas.readLine()) != null) {

						final String[] array_iledefrance = currentarea.split(";");
						array_iledefrance[1] = array_iledefrance[1].replaceAll("\"\"", "\"");
						array_iledefrance[1] = array_iledefrance[1].substring(1, array_iledefrance[1].length());
						final Polygon polygon = gjson.readPolygon(array_iledefrance[1]);

						try {
							if (polygon.intersects(line)) {
								writer.println(array_paris[2] + ";" + array_iledefrance[0]);
								break;
							}
						} catch (final Exception e) {
							e.getMessage();
						}

					}
					areas.close();
				} catch (final Exception e) {
					e.getMessage();
				}
			}
			roads.close();
			writer.close();
		} catch (final Exception e) {
			// nothing
		}

	}

}
