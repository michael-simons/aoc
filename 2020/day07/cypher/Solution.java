///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 15
//DEPS org.neo4j:neo4j:4.2.1

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.internal.unsafe.UnsafeUtil;

public final class Solution {

	public static void main(String... args) throws Exception {

		var start = System.currentTimeMillis();
		UnsafeUtil.disableIllegalAccessLogger();
		var dbms = new DatabaseManagementServiceBuilder(Files.createTempDirectory("neo4j")).build();
		var database = dbms.database("neo4j");
		database.executeTransactionally("CREATE INDEX bag_type FOR (n:Bag) ON (n.type)");
		var duration = Duration.ofMillis(System.currentTimeMillis() - start);
		System.out.println("Neo4j running after %s".formatted(duration));

		start = System.currentTimeMillis();
		try (var tx = database.beginTx()) {
			var pattern = Pattern.compile("(\\d+)? ?([a-z\\s]+) bags?");

			Files.readAllLines(Path.of(args.length == 0 ? "input.txt" : args[0]))
				.stream()
				.map(pattern::matcher)
				.map(m -> m.results()
					.map(r -> Map.of("num", r.group(1) == null ? 0 : Integer.valueOf(r.group(1)), "type", r.group(2).trim()))
					.collect(Collectors.toList())
				)
				.forEach(bags ->
					tx.execute("""
							MERGE (outer:Bag {type: head($bags).type})
							WITH outer UNWIND tail($bags) AS bag
							MERGE (inner:Bag {type: bag.type})
							MERGE (outer) - [:CONTAINS {num: bag.num}] -> (inner)
							""",
						Map.of("bags", bags)
					).close()
				);
			tx.commit();
		}
		duration = Duration.ofMillis(System.currentTimeMillis() - start);
		System.out.println("Model loaded and stored in %s".formatted(duration));

		try (var tx = database.beginTx()) {

			start = System.currentTimeMillis();
			var starOne = tx.execute("""
				MATCH (bag)-[:CONTAINS*1..]->(:Bag {type:'shiny gold'})
				RETURN count(DISTINCT bag) AS starOne
				""").next().get("starOne");
			duration = Duration.ofMillis(System.currentTimeMillis() - start);
			System.out.println("Star one %d computed in %s".formatted(starOne, duration));

			start = System.currentTimeMillis();
			var starTwo = tx.execute("""
				MATCH p=(b:Bag {type:'shiny gold'})-[:CONTAINS*1..]->(otherBag)
				WITH otherBag, reduce(cnt = 1, rel IN relationships(p) | cnt * rel.num) as total
				RETURN sum(total) AS starTwo
				""").next().get("starTwo");
			duration = Duration.ofMillis(System.currentTimeMillis() - start);
			System.out.println("Star two %d computed in %s".formatted(starTwo, duration));

			tx.commit();
		}

		dbms.shutdown();
	}
}
