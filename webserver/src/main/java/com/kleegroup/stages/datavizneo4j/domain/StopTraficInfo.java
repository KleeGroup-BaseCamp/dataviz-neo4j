package com.kleegroup.stages.datavizneo4j.domain;

public final class StopTraficInfo {

	private final String name;
	private final int nb_vald; // par jour/heure
	private final float nb_disturbances; // moyen par jour
	private final float perc_on_time;
	private final float perc_delayed;
	private final float perc_early;
	private final float perc_cancelled;
	private final float perc_unknown;
	private final float time_delay; // moyen

	public StopTraficInfo(final String name, final int nb_vald, final float nb_disturbances, final float perc_on_time, final float perc_delayed, final float perc_early, final float perc_cancelled, final float perc_unknown, final float time_delay) {
		this.name = name;
		this.nb_vald = nb_vald;
		this.nb_disturbances = nb_disturbances; // par mois
		this.perc_delayed = perc_delayed;
		this.perc_on_time = perc_on_time;
		this.perc_early = perc_early;
		this.perc_cancelled = perc_cancelled;
		this.perc_unknown = perc_unknown;
		this.time_delay = time_delay; // en millisecondes

	}

	public String getName() {
		return name;
	}

	public int getNb_vald() {
		return nb_vald;
	}

	public float getNb_disturbances() {
		return nb_disturbances;
	}

	public float getPerc_on_time() {
		return perc_on_time;
	}

	public float getPerc_delayed() {
		return perc_delayed;
	}

	public float getPerc_early() {
		return perc_early;
	}

	public float getPerc_cancelled() {
		return perc_cancelled;
	}

	public float getPerc_unknown() {
		return perc_unknown;
	}

	public float getTime_delay() {
		return time_delay;
	}

}
