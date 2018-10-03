/*
 * Nom de classe : RoadSection_Data_Converter
 *
 * Description   : Listes des dates des vacances et jours fériés pour le premier semestre de 2017 
 * 					Inclus une méthode permettant de savoir à quel type de jour appartient
 *					une certaine date
 *
 * Version       : 1.0
 *
 * Date          : 23/04/2018
 * 
 * Copyright     : 
 */
package data_Tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.javatuples.Pair;

@SuppressWarnings("deprecation")
public class Dates {

	/**
	 * Jours fériés
	 */
	private static ArrayList<Date> publicholidays = new ArrayList<Date>(Arrays.asList(new Date(117, 00, 01), new Date(117, 03, 17), new Date(117, 04, 01),
			new Date(117, 04, 8), new Date(117, 04, 25), new Date(117, 05, 05), new Date(117, 06, 14), new Date(117, 7, 15), new Date(117, 10, 1), new Date(117, 11, 25)));
	/**
	 * Vacances Scolaires
	 */
	private static ArrayList<Pair<Date, Date>> schoolholidays = new ArrayList<>(Arrays.asList(Pair.with(new Date(117, 00, 01), new Date(117, 00, 03)), Pair.with(new Date(117, 01, 04), new Date(117, 01, 19)), Pair.with(new Date(117, 03, 01), new Date(117, 03, 17)), Pair.with(new Date(117, 04, 24), new Date(117, 04, 28)), Pair.with(new Date(117, 06, 8), new Date(117, 8, 03)),
			Pair.with(new Date(117, 9, 21), new Date(117, 10, 05)), Pair.with(new Date(117, 11, 23), new Date(117, 11, 23))));

	/**
	 * Fonction permettant de trouver la catégorie de jour à laquelle une date correspond.
	 * @param 	date	La date dont on veut trouver le catégorie
	 * @return Une chaîne de caractère correspondant au type de jour (JOHV,JOVS,SAHV,SAVS,DIJFP)
	 */
	public static String DateType(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		if (calendar.get(Calendar.DAY_OF_WEEK) == 1) {
			return "DIJFP";
		}
		for (Date ph : publicholidays) {
			if (date == ph) {
				return "DIJFP";
			}
		}
		for (Pair<Date, Date> dates : schoolholidays) {
			if (date.after(dates.getValue0()) && date.before(dates.getValue1())) {
				if (calendar.get(Calendar.DAY_OF_WEEK) == 7) {
					return "SAVS";
				} else {
					return "JOVS";
				}
			}
		}
		if (calendar.get(Calendar.DAY_OF_WEEK) == 7) {
			return "SAHV";
		} else {
			return "JOHV";
		}
	}

	public static int Convert_DateType_int(String dateType) {
		switch (dateType) {
			case "JOHV":
				return 0;
			case "JOVS":
				return 1;
			case "SAHV":
				return 2;
			case "SAVS":
				return 3;
			case "DIJFP":
				return 4;
			default:
				return -1;
		}
	}

	public static String Convert_int_DateType(int type) {
		switch (type) {
			case 0:
				return "JOHV";
			case 1:
				return "JOVS";
			case 2:
				return "SAHV";
			case 3:
				return "SAVS";
			case 4:
				return "DIJFP";
			default:
				return "";
		}
	}
}
