MATCH (n) DETACH DELETE n;

LOAD CSV FROM 'file:///input.txt' AS line
WITH linenumber() AS x, split(line[0], '') AS cols
UNWIND range(0, size(cols) -1) AS y
CREATE (:Tile {x: x, y: y+1, n: toInteger(cols[y])});

MATCH (t1:Tile)
WITH t1, [1, 0, -1, 0] AS dx, [0, 1, 0, -1] AS dy
UNWIND range(0, size(dx)-1) AS i
MATCH (t2:Tile {x: t1.x+dx[i], y: t1.y+dy[i]})
CREATE (t1)-[:LINKED_TO {w: t2.n - t1.n}]->(t2);

MATCH (s:Tile {n:0})
MATCH SHORTEST 1 (s)-[:LINKED_TO {w:1}]->*(e {n: 9})
WITH s, count(e) AS endpoints
WITH sum(endpoints) AS star1
MATCH (s:Tile {n:0})
MATCH p=(s)-[:LINKED_TO {w:1}]->*({n: 9})
WITH star1, s, count(p) AS trails
RETURN star1, sum(trails) as star2;
