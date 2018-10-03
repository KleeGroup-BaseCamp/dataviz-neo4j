package com.kleegroup.stages.datavizneo4j.services;

import io.vertigo.core.component.Component;

public interface RouteServices extends Component {

	/**
	 * Permet de combiner tous les morceaux de tracés dans le fichier source pour assembler la ligne complète
	 * et crée un fichier regroupant chaque associations de tracé/ligne.
	 */
	void MultiLinesCreation();
}
