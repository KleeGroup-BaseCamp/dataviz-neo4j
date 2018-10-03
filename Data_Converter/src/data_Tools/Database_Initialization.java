package data_Tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

public class Database_Initialization {

	public static void first() {
		Driver driver = GraphDatabase.driver("bolt://localhost", AuthTokens.basic("neo4j", "oayoub"));
		try (Session session = driver.session()) {
			StatementResult rs = session.run("match (n:RoadSection) WHERE EXISTS(n.geoshape) AND EXISTS(n.geopoint) AND EXISTS(n.id_arc_tra) RETURN n");
			while (rs.hasNext()) {
				Record record = rs.next();
				try {
					System.out.println(Integer.parseInt(record.get(0).get("id_arc_tra").toString()));
					Integer.parseInt(record.get(0).get("id").toString());
					Integer.parseInt(record.get(0).get("id_arc_tra").toString());
					record.get(0).get("geoshape");
					record.get(0).get("geopoint");
				} catch (Exception e) {
					System.out.println(Integer.parseInt(record.get(0).get("id").toString()));
					System.out.println(Integer.parseInt(record.get(0).get("id_arc_tra").toString()));
					System.out.println(record.get(0).get("geoshape"));
					System.out.println(record.get(0).get("geopoint"));
					e.printStackTrace();
				}

			}
		}
		driver.close();
	}

	public static void importation() throws IOException {
		File file = new File("src/data_Tools/trafic_capteurs_2017_1er1.txt");
		Driver driver = GraphDatabase.driver("bolt://localhost", AuthTokens.basic("neo4j", "oayoub"));
		try (Session session = driver.session()) {
			File file1 = new File("C:/Users/oayoub/Desktop/neo4j-community-3.3.5/import/trafic_capteurs_2017_1er1.txt");
			Files.copy(file.toPath(), file1.toPath(), StandardCopyOption.REPLACE_EXISTING);
			session.run("load csv with headers from  \r\n" +
					" 'file:///trafic_capteurs_2017_1er1.txt' as csv  \r\n" +
					" match (n:RoadSection {id_arc_tra: toInt(csv.id)})-->(m:CatDay{type:csv.type}) RETURN n,m  ");
		}
		driver.close();
	}

}
