package com.kleegroup.stages.datavizneo4j.services;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

import io.vertigo.core.component.Activeable;
import io.vertigo.core.component.Component;

public class Neo4JDriverProvider implements Component, Activeable {

	private final Driver neo4jDriver;

	@Inject
	public Neo4JDriverProvider() {
		final Driver myNeo4jDriver = GraphDatabase.driver("bolt://172.20.85.200", AuthTokens.basic("neo4j", "dataviz"), Config.build()
				.withConnectionTimeout(3, TimeUnit.MINUTES)
				.withConnectionLivenessCheckTimeout(10, TimeUnit.MINUTES)
				.withConnectionAcquisitionTimeout(10, TimeUnit.MINUTES)
				.withMaxConnectionPoolSize(10000)
				.toConfig());
		// ---
		neo4jDriver = myNeo4jDriver;
	}

	public Driver getNeo4jDriver() {
		return neo4jDriver;
	}

	@Override
	public void start() {
		// nothing to do

	}

	@Override
	public void stop() {
		neo4jDriver.close();

	}

}
