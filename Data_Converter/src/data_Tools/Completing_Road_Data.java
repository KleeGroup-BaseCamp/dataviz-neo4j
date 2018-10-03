/*
 * Nom de classe : Completing_Road_Data
 *
 * Description   : Regroupe les fonctions permettant de completer les données 
 * 					liées au trafic routier.
 *
 * Version       : 1.0
 *
 * Date          : 15/05/2018
 * 
 * Copyright     : 
 */
package data_Tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.javatuples.Pair;

public class Completing_Road_Data {
	/**
	 * Crée un fichier avec pour chaque section de route, catégorie de jour et créneau horaire, les coeffecients directeurs des
	 * régressions polynomiales du deuxième et troisième ordre des valeurs correspondantes. Les regressions sont calculées seulement si le nombre de couple
	 * de valeurs est supérieur à 20.
	 */
	public static void road_regression() {
		PrintWriter writer = null;
		BufferedReader reader = null;
		try {
			writer = new PrintWriter("src/data_Tools/trafic_capteurs_2017_1er_regression.txt", "UTF-8");
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		@SuppressWarnings("unchecked")
		Pair<ArrayList<Double>, ArrayList<Double>>[][] matrice = new Pair[5][6998]; //matrice des listes de taux pour chaque type de jour (JOHV,JOVS,SAHS,SAVS,DIJFP)

		for (int i = 0; i < 5; i++) {
			for (int k = 0; k < 6998; k++) {
				matrice[i][k] = new Pair<ArrayList<Double>, ArrayList<Double>>(new ArrayList<Double>(), new ArrayList<Double>());
			}
		}
		for (int month = 1; month < 7; month++) { //taux par semestre
			String test = null;
			try {
				reader = new BufferedReader(new FileReader("src/data_Tools/donnees_trafic_capteurs_20170" + month + ".txt"));
				test = reader.readLine();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			int nb_roads_month = RoadSection_Data_Converter.nb_RoadSection_month(month);
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			for (int id = 1; id < nb_roads_month - 3; id++) {
				try {
					String[] array1 = test.split("	");
					Date t = ft.parse(array1[1]);
					calendar.setTime(t);
					int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
					for (int i = 0; i < daysInMonth; i++) {
						t = ft.parse(array1[1]);
						String type = Dates.DateType(t);
						int line;
						if (type == "JOHV") {
							line = 0;
						} else if (type == "JOVS") {
							line = 1;
						} else if (type == "SAHV") {
							line = 2;
						} else if (type == "SAVS") {
							line = 3;
						} else {
							line = 4;
						}
						int id1 = Integer.parseInt(array1[0]);
						for (int j = 0; j < 24; j++) {
							if (test == null) {
								break;
							}
							array1 = test.split("	");
							t = ft.parse(array1[1]);
							try {
								if (array1[2].length() == 0) {
									throw new Exception();
								}
								array1[2] = array1[2].replace(',', '.');
								array1[3] = array1[3].replace(',', '.');
								if (array1[3].length() != 0) {
									((ArrayList<Double>) matrice[line][id1 - 1].getValue0()).add(Double.parseDouble(array1[3]));
									((ArrayList<Double>) matrice[line][id1 - 1].getValue1()).add(Double.parseDouble(array1[2]));
								}
							} catch (Exception e) {
							}
							String test1 = test;
							if ((test = reader.readLine()) == null) {
								System.out.println(test1);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		//SimpleRegression reg=new SimpleRegression(false);
		for (int k = 0; k < 6998; k++) {
			for (int i = 0; i < 5; i++) {
				if (matrice[i][k].getValue1().size() > 20) {
					ArrayList<Integer> under_index = new ArrayList<Integer>();
					ArrayList<Integer> above_index = new ArrayList<Integer>();
					Double critical = Collections.max(matrice[i][k].getValue1());
					Double critical_to = matrice[i][k].getValue0().get(matrice[i][k].getValue1().indexOf(critical));
					int counter = 0;
					for (Double d : matrice[i][k].getValue0()) {
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
					/*System.out.println(i);
					System.out.println(matrice[i][k].getValue0());
					System.out.println(matrice[i][k].getValue1());*/
					WeightedObservedPoints obs = new WeightedObservedPoints();
					obs.add(1000, 0, 0);
					PolynomialCurveFitter fitter = PolynomialCurveFitter.create(3);
					for (int y = 0; y < matrice[i][k].getValue1().size(); y++) {
						obs.add(matrice[i][k].getValue0().get(y), matrice[i][k].getValue1().get(y));
					}
					double[] coeff = fitter.fit(obs.toList());
					obs.clear();
					double[] coeff_above = null;
					if (above_index.size() > 20) {
						fitter = PolynomialCurveFitter.create(2);
						for (Integer index : above_index) {
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

					/*reg.clear();
					for(int x=0;x<matrice[i][k].getValue0().size();x++) {
						reg.addData(matrice[i][k].getValue0().get(x),matrice[i][k].getValue1().get(x));
					}*/
					String type;
					if (i == 0) {
						type = "JOHV";
					} else if (i == 1) {
						type = "JOVS";
					} else if (i == 2) {
						type = "SAHV";
					} else if (i == 3) {
						type = "SAVS";
					} else {
						type = "DIJFP";
					}
					if (above_index.size() > 10) {
						writer.println((k + 1) + "," + type + "," + critical_to + "," + coeff[0] + ";" + coeff[1] + ";" + coeff[2] + ";" + coeff[3] + "," + coeff_above[0] + ";" + coeff_above[1] + ";" + coeff_above[2]);
					} else {
						writer.println((k + 1) + "," + type + "," + critical_to + "," + coeff[0] + ";" + coeff[1] + ";" + coeff[2] + ";" + coeff[3]);
					}

				}
			}
		}
		try {
			reader.close();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Permet de trouver la ligne correspondante aux paramètres recherchés dans le fichier concernant
	 * les données sur les régréssions linéaires et de renvoyer la liste des éléments des régréssions
	 * calculées.
	 * 
	 * @param id	Id de la section de route recherchée
	 * @param type	Catégorie de jour recherchée 
	 * @return	Une liste de Double représentant le taux d'occupation critique, les coeffecients de l'équation de regression
	 *          polynomiale de troisième ordre qui convient pour les données situées en dessous du taux critique et enfin
	 *          les coefficients de l'équation de regression polynomiale de second ordre qui convient pour les données
	 *          situées au dessous du taux critique.
	 * 
	 */
	public static ArrayList<Double> search_regression(int id, String type) {
		BufferedReader reader = null;
		String test;
		ArrayList<Double> result = new ArrayList<Double>();
		try {
			reader = new BufferedReader(new FileReader("src/data_Tools/trafic_capteurs_2017_1er_regression.txt"));
			while ((test = reader.readLine()) != null) {
				String[] array1 = test.split(",");
				try {
					array1[4].length();
				} catch (Exception e1) {
					if (Integer.parseInt(array1[0]) == id && array1[1].equals(type)) {
						result.add(Double.parseDouble(array1[2]));
						String[] array2 = array1[3].split(";");
						for (String temp : array2) {
							result.add(Double.parseDouble(temp));
						}
						reader.close();
						return result;
					}
				}
				if (Integer.parseInt(array1[0]) == id && array1[1].equals(type)) {
					result.add(Double.parseDouble(array1[2]));
					String[] array3 = array1[3].split(";");
					for (String temp : array3) {
						result.add(Double.parseDouble(temp));
					}
					String[] array4 = array1[4].split(";");
					for (String temp : array4) {
						result.add(Double.parseDouble(temp));
					}
					reader.close();
					return result;
				}
				if (Integer.parseInt(array1[0]) > id) {
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			reader.close();
		} catch (IOException e) {
		}
		return null;
	}

	/**
	 * Lis, pour chaque mois, chaque ligne des fichiers de données traffics brutes et les lignes des données complétées. Si la date
	 * et l'id correspond alors il écrit la ligne du fichier des données complétées, sinon il écrit la ligne de l'ancien fichier.
	 * 
	 * @param month un entier qui désigne le mois à fusionner
	 */
	public static void merging(int month) {
		BufferedReader reader_origin = null;
		BufferedReader reader_correction = null;
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("src/data_Tools/trafic_capteurs_2017_fulldata_20170" + month + ".txt", "UTF-8");
			reader_origin = new BufferedReader(new FileReader("src/data_Tools/donnees_trafic_capteurs_20170" + month + ".txt"));
			reader_correction = new BufferedReader(new FileReader("src/data_Tools/trafic_capteurs_2017_first_completion_20170" + month + ".txt"));
			String origin = null;
			String correction = null;
			correction = reader_correction.readLine();
			String[] array2 = correction.split("	");
			while ((origin = reader_origin.readLine()) != null) {
				String[] array1 = origin.split("	");
				if (array1[0].equals(array2[0]) && array1[1].equals(array2[1])) {
					writer.println(correction);
					if ((correction = reader_correction.readLine()) != null) {
						array2 = correction.split("	");
					}
				} else {
					writer.println(origin);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			reader_origin.close();
			reader_correction.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Pour chaque ligne des données brutes, si il manque une des deux données (débit ou taux), récupère les données des régressions si
	 * elles existent et complete les données puis écrit un fichier avec toutes les lignes complétées.
	 * 
	 */
	public static void first_completion() {
		BufferedReader reader = null;
		PrintWriter writer = null;

		//BufferedReader reader_regression=null;
		for (int month = 1; month < 2; month++) { //taux par semestre
			String test = null;
			try {
				writer = new PrintWriter("src/data_Tools/trafic_capteurs_2017_first_completion_20170" + month + ".txt", "UTF-8");
				reader = new BufferedReader(new FileReader("src/data_Tools/donnees_trafic_capteurs_20170" + month + ".txt"));
				test = reader.readLine();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			int nb_roads_month = RoadSection_Data_Converter.nb_RoadSection_month(month);
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			for (int id = 1; id < nb_roads_month - 3; id++) {
				try {
					String[] array1 = test.split("	");
					Date t = ft.parse(array1[1]);
					calendar.setTime(t);
					int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
					for (int i = 0; i < daysInMonth; i++) {
						int id1 = Integer.parseInt(array1[0]);
						for (int j = 0; j < 24; j++) {
							if (test == null) {
								break;
							}
							array1 = test.split("	");
							t = ft.parse(array1[1]);
							String type = Dates.DateType(t);
							try {
								if (array1[2].length() > 0 && Double.parseDouble(array1[2]) > 0) {
									ArrayList<Double> result = search_regression(id1, type);
									try {
										array1[3].length();
									} catch (Exception p) {
										Double to;
										//taux d'occupation inconnu
										if ((j >= 6 && j < 10) || (j >= 16 && j < 21)) {
											//dense					
											ArrayList<Double> coeff = new ArrayList<Double>();
											coeff.add(result.get(5));
											coeff.add(result.get(6));
											coeff.add(result.get(7));
											to = (double) Math.round(Polynomial_Solver.solver(coeff, Double.parseDouble(array1[2])) * 100.0) / 100.0;
										} else {
											//fluide
											ArrayList<Double> coeff = new ArrayList<Double>();
											coeff.add(result.get(1));
											coeff.add(result.get(2));
											coeff.add(result.get(3));
											coeff.add(result.get(4));
											to = (double) Math.round(Polynomial_Solver.solver(coeff, Double.parseDouble(array1[2])) * 100.0) / 100.0;
										}
										String line = String.join("	", array1);
										line += "	" + to;
										if (to != null) {
											writer.println(line);
										}
									}
								} else {
									//debit inconnu
									array1[3] = array1[3].replace(',', '.');
									if (Double.parseDouble(array1[3]) > 0.0) {
										ArrayList<Double> result = search_regression(id1, type);
										Double max = result.get(0);
										Double x = Double.parseDouble(array1[3]);
										if (x <= max) {
											int debit = (int) (Math.round((result.get(1) + x * result.get(2) + Math.pow(x, 2) * result.get(3) + Math.pow(x, 3) * result.get(4)) * 100.0) / 100.0);
											array1[2] = debit + "";
											String line = String.join("	", array1);
											if (debit > 0) {
												writer.println(line);
											}
										} else {
											try {
												result.get(5);
												int debit = (int) (Math.round((result.get(5) + x * result.get(6) + Math.pow(x, 2) * result.get(7)) * 100.0) / 100.0);
												array1[2] = debit + "";
												String line = String.join("	", array1);
												if (debit > 0) {
													writer.println(line);
												}
											} catch (Exception p) {
											}
										}
									}
								}
							} catch (Exception e) {
							}
							String test1 = test;
							if ((test = reader.readLine()) == null) {
								System.out.println(test1 + "  yo");
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				reader.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
