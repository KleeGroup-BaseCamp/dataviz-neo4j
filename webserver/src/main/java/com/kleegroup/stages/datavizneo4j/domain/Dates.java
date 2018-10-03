package com.kleegroup.stages.datavizneo4j.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.javatuples.Pair;

@SuppressWarnings("deprecation")
public final class Dates {

	/**
	 * Jours fériés
	 */
	private static ArrayList<Date> publicholidays = new ArrayList<>(Arrays.asList(new Date(117, 00, 01), new Date(117, 03, 17), new Date(117, 04, 01),
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
	public static DateType DateType(final Date date) {
		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		if (calendar.get(Calendar.DAY_OF_WEEK) == 1) {
			return DateType.DIJFP;
		}
		for (final Date ph : publicholidays) {
			if (date == ph) {
				return DateType.DIJFP;
			}
		}
		for (final Pair<Date, Date> dates : schoolholidays) {
			if (date.after(dates.getValue0()) && date.before(dates.getValue1())) {
				if (calendar.get(Calendar.DAY_OF_WEEK) == 7) {
					return DateType.SAVS;
				} else {
					return DateType.JOVS;
				}
			}
		}
		if (calendar.get(Calendar.DAY_OF_WEEK) == 7) {
			return DateType.SAHV;
		} else {
			return DateType.JOHV;
		}
	}

	public static int Convert_DateType_int(final DateType dateType) {
		switch (dateType) {
			case JOHV:
				return 0;
			case JOVS:
				return 1;
			case SAHV:
				return 2;
			case SAVS:
				return 3;
			case DIJFP:
				return 4;
			default:
				return -1;
		}
	}

	public static DateType Convert_int_DateType(final int type) {
		switch (type) {
			case 0:
				return DateType.JOHV;
			case 1:
				return DateType.JOVS;
			case 2:
				return DateType.SAHV;
			case 3:
				return DateType.SAVS;
			case 4:
				return DateType.DIJFP;
			default:
				return null;
		}
	}
}
