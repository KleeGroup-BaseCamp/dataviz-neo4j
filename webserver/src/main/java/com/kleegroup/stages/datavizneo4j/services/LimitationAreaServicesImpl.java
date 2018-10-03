package com.kleegroup.stages.datavizneo4j.services;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import com.kleegroup.stages.datavizneo4j.domain.LimitationArea;

public class LimitationAreaServicesImpl implements LimitationAreaServices {

	@Inject
	private Neo4JDriverProvider neo4JDriverProvider;

	@Override
	public List<LimitationArea> getAll() {

		final List<LimitationArea> result = new ArrayList<>();
		final Driver driver = neo4JDriverProvider.getNeo4jDriver();

		try (Session session = driver.session()) {
			final StatementResult rs = session.run("match (n:LimitationArea) RETURN n");

			while (rs.hasNext()) {

				final Record record = rs.next();
				final String record_speed = record.get(0).get("speed_limitation").toString();
				final int int_speed;
				if (record_speed.equals("\"30\"")) {
					int_speed = 30;
				} else {
					int_speed = 20;
				}
				result.add(new LimitationArea(Integer.parseInt(record.get(0).get("id").toString()),
						record.get(0).get("polygon_shape").toString(),
						int_speed));
			}
		}
		return result;
	}

}
