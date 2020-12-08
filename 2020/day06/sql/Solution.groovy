@GrabConfig(systemClassLoader = true)
@Grab('com.h2database:h2:1.4.200')

import groovy.sql.Sql

class Solution {
    static void main(String... args) {

        Sql.withInstance(url: 'jdbc:h2:mem:', driver: 'org.h2.Driver') { sql ->

            sql.execute """
                CREATE TABLE groups(id SERIAL PRIMARY KEY);
                CREATE TABLE people(
                    id SERIAL PRIMARY KEY, 
                    group_id BIGINT UNSIGNED NOT NULL,
                    CONSTRAINT group_fk FOREIGN KEY (group_id) REFERENCES groups (id)
                );
                CREATE TABLE answers(
                    id SERIAL PRIMARY KEY, 
                    person_id BIGINT UNSIGNED NOT NULL,
                    value VARCHAR(1) NOT NULL,
                    CONSTRAINT person_fk FOREIGN KEY (person_id) REFERENCES people (id)
                );
            """

            def lastGroupId;
            File.newInstance('input.txt').eachLine { line, number ->
                if (number == 1 || line.isBlank()) {
                    lastGroupId = sql.executeInsert('INSERT INTO groups VALUES()')[0][0]
                }
                if (line.isBlank()) {
                    return
                }

                def personId = sql.executeInsert('INSERT INTO people(group_id) VALUES (?)', lastGroupId)[0][0]
                sql.withBatch(26, 'INSERT INTO answers(person_id, value) VALUES(?, ?)') { stmt ->
                    line.split("").each { value ->
                        stmt.addBatch(personId, value)
                    }
                }
            }

            def starOne = sql.firstRow """
                WITH source AS (
                    SELECT group_id, count(DISTINCT value) number_of_yes 
                    FROM answers a 
                    INNER JOIN people p ON p.id = a.person_id 
                    GROUP by group_id
                ) 
                SELECT sum(number_of_yes) FROM source;                
            """
            println "Star one: ${starOne[0]}"

            def starTwo = sql.firstRow """
                WITH g AS (
                    SELECT group_id, count(distinct p.id) as yes_per_group
                    FROM answers a 
                    INNER JOIN people p ON p.id = a.person_id
                    GROUP BY group_id
                ),
                gv AS (
                    SELECT group_id, value, count(*) as yes_per_group_and_value
                    FROM answers a 
                    INNER JOIN people p ON p.id = a.person_id
                    GROUP BY group_id, value
                ) 
                SELECT count(*) 
                FROM g, gv
                WHERE g.yes_per_group = gv.yes_per_group_and_value
                 AND g.group_id = gv.group_id                
            """
            println "Star two: ${starTwo[0]}"
        }
    }
}
