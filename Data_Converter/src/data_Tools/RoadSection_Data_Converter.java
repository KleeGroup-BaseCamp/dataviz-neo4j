/*
 * Nom de classe : RoadSection_Data_Converter
 *
 * Description   : Regroupe les fonctions permettant de lire et de convertir les 
 * 					données liées aux sections de route sur Paris.
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import org.javatuples.Pair;


public class RoadSection_Data_Converter {

	/**
	 * Permet de calculer la moyenne de Double dans une liste.
	 * 
	 * @param rates Liste de Double pour laquelle la moyenne va être calculer
	 * @return La moyenne des Double dans la liste
	 */
	public static double calculateAverage(ArrayList <Double> rates) {
		Double sum = 0.0;
		if(!rates.isEmpty()) {
			for (Double mark : rates) {
				sum += mark;
			}
			return Math.round(sum.doubleValue() / rates.size() * 100.0) / 100.0;
		}
		return sum;
	}


	/**
	 * Permet de calculer la vitesse sur une section de route.
	 * 
	 * @param 		rate		Le taux d'occupation relevé sur la section 
	 * @param 		rateflow	Le débit relevé sur le section 
	 * @param 		distance	La longueur d'une véhicule en moyenne + La longueur du capteur au sol	
	 * @return
	 */
	public static double calculateSpeed(Double rate,Double rateflow,Double distance) {
		return Math.round((distance*rateflow/(rate*100)*3.6)*100.0)/100.0;
	}

	/**
	 * Permet de trouver le nombre de section de route différent en analysant le nombre d'id différents
	 * dans les fichiers
	 * @return Le maximun de section de route sur l'ensemble des fichiers 
	 */
	public static int nb_RoadSection() {
		BufferedReader reader=null;
		int currentnumber=0;
		int currentmax=0;
		int currentid=0;
		for(int month=1;month<6;month++){
			String test=null;
			try {
				reader= new BufferedReader(new FileReader("src/test/donnees_trafic_capteurs_20170"+month+".txt"));
				test=reader.readLine();
				String[] array1 = test.split("	");
				currentid=Integer.parseInt(array1[0]);
				while((test=reader.readLine())!=null) {
					array1 = test.split("	");
					if(Integer.parseInt(array1[0].toString())!=currentid) {
						currentnumber++;
						currentid=Integer.parseInt(array1[0]);
					}
				}
				if(currentnumber>currentmax) {
					currentmax=currentnumber;
					
				}
				currentnumber=0;
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		return currentmax;
	}
	public static int nb_RoadSection_month(int month) {
		BufferedReader reader=null;
		int currentnumber=0;
		int currentid=0;
		String test=null;
		try {
			reader= new BufferedReader(new FileReader("src/data_Tools/donnees_trafic_capteurs_20170"+month+".txt"));
			test=reader.readLine();
			String[] array1 = test.split("	");
			currentid=Integer.parseInt(array1[0]);
			while((test=reader.readLine())!=null) {
				array1 = test.split("	");
				if(Integer.parseInt(array1[0].toString())!=currentid) {
					currentnumber++;
					currentid=Integer.parseInt(array1[0]);
				}
			}	
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return currentnumber;
	}

	/**
	 * Permet de lire chaque fichier ( un nom standardisé est requis ) qui contient les informations
	 * concernant les débits et les taux d'occupations des sections de routes sur Paris. Un fichier est ensuite créé
	 * contenant les moyennes des débits, taux d'occupations et vitesses pour chaque section et pour chaque type
	 * jour (JOHV,JOVS,SAHV,SAVS,DIJFP). Chaque type de jour est divisé en créneau horaire de une heure. 
	 */
	public static void dataconverter() {
		PrintWriter writer = null;
		BufferedReader reader=null;
		try {
			writer = new PrintWriter("src/data_Tools/trafic_capteurs_2017_1er1.txt", "UTF-8");
			writer.println("id,type,TRNC_HORR,rate,rateflow,speed");
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		@SuppressWarnings("unchecked")
		Pair<ArrayList<Double>,ArrayList<Double>>[][][] matrice=new Pair[6][24][6998]; //matrice des listes de taux pour chaque type de jour (JOHV,JOVS,SAHS,SAVS,DIJFP)

		for(int i=0;i<6;i++) {
			for(int j=0;j<24;j++) {
				for(int k=0;k<6998;k++) {
					matrice[i][j][k]=new Pair<ArrayList<Double>,ArrayList<Double>>(new ArrayList<Double>(),new ArrayList<Double>());
				}
			}
		}
		for(int month=1;month<7;month++){ //taux par semestre
			String test=null;
			try {
				reader= new BufferedReader(new FileReader("src/data_Tools/donnees_trafic_capteurs_20170"+month+".txt"));
				test=reader.readLine();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			int nb_roads_month=nb_RoadSection_month(month);
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");

			for(int id=1;id<nb_roads_month-3;id++) {//  les id ne sont pas dans le bon ordre et ils manquents des données dans le fichier
				try {
					String[] array1 = test.split("	");
					Date t=ft.parse(array1[1]);
					calendar.setTime(t);
					int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
					for(int i=0;i<daysInMonth;i++) {
						t=ft.parse(array1[1]);
						String type=Dates.DateType(t);
						int line;
						if(type=="JOHV") {
							line=0;
						}
						else if(type=="JOVS") {
							line=1;
						}
						else if(type=="SAHV") {
							line=2;
						}
						else if(type=="SAVS") {
							line=3;
						}
						else {
							line=4;
						}
						int id1=Integer.parseInt(array1[0]);
						for(int j=0;j<24;j++) {
							if(test==null) {
								break;
							}
							array1 = test.split("	");
							t=ft.parse(array1[1]);
							try {
								if(array1[2].length()==0) {
									throw new Exception();
								}
								array1[2]=array1[2].replace(',', '.');
								array1[3]=array1[3].replace(',', '.');
								if(array1[3].length()!=0) {
									((ArrayList<Double>) matrice[line][j][id1-1].getValue0()).add(Double.parseDouble(array1[3]));
									((ArrayList<Double>) matrice[line][j][id1-1].getValue1()).add(Double.parseDouble(array1[2]));
								}
							}
							catch(Exception e) {
							}
							String test1=test;
							if((test=reader.readLine())==null) {
								System.out.println(test1);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		for(int k=0;k<6998;k++) {
			for(int i=0;i<6;i++) {
				for(int j=0;j<24;j++) {

					if(!matrice[i][j][k].getValue1().isEmpty() && !matrice[i][j][k].getValue0().isEmpty()) {
						String type;
						if(i==0) {
							type="JOHV";
						}
						else if(i==1) {
							type="JOVS";
						}
						else if(i==2) {
							type="SAHV";
						}
						else if(i==3) {
							type="SAVS";
						}
						else {
							type="DIJFP";
						}
						Double average_rate=calculateAverage(matrice[i][j][k].getValue0());
						Double average_rateflow=calculateAverage(matrice[i][j][k].getValue1());
						Double average_speed=calculateSpeed(average_rate, average_rateflow, 4.5);
						if(j==23) {
							writer.println((k+1)+","+type+",23H-0H,"+average_rate+","+average_rateflow+","+average_speed);
						}
						else {
							writer.println((k+1)+","+type+","+j+"H-"+(j+1)+"H,"+average_rate+","+average_rateflow+","+average_speed);
						}	
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
}
