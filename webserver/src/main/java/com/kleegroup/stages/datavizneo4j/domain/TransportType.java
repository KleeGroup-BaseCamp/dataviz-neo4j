package com.kleegroup.stages.datavizneo4j.domain;

import java.util.stream.Stream;

import io.vertigo.lang.VSystemException;

public enum TransportType {
	TRAMWAY(0),
	METRO(1),
	TRAIN(2),
	BUS(3),
	FUNICULAIRE(7);

	private final int typeStored;

	private TransportType(final int typeStored) {
		this.typeStored = typeStored;
	}

	public int getTypeStored() {
		return typeStored;
	}

	public static TransportType getTransportTypeFromId(final int id) {
		return Stream.of(TransportType.values())
				.filter(transportType -> transportType.getTypeStored() == id)
				.findFirst()
				.orElseThrow(() -> new VSystemException("Unable to find transport type with id  : '{0}' ", id));
	}

}
