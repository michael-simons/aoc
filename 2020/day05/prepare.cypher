LOAD csv FROM 'file:///input.txt' AS lines
UNWIND lines AS line 
WITH split(line,'') AS steps
UNWIND range(0, size(steps)-2) as i
CREATE (n) WITH collect(n) as nodes, steps
CREATE (s:Start), (e:Seat) 
WITH s + nodes + e as nodes, steps
UNWIND range(0, size(steps)-1) as i
WITH steps[i] as t,  nodes[i] as node1, nodes[i+1] as node2 
CALL apoc.create.relationship(node1, t, {}, node2) YIELD rel
RETURN COUNT(rel) as relationshipsCreated;
