WITH 
  input AS (SELECT column0, column3 FROM read_csv('/dev/stdin', header=false, delim=' ')),
  l1 AS (SELECT row_number() OVER (ORDER BY column0) as row, column0 AS v FROM input),
  l2 AS (SELECT row_number() OVER (ORDER BY column3) as row, column3 AS v FROM input),
  pt1 AS (SELECT sum(abs(l2.v - l1.v)) AS v FROM l1 JOIN l2 USING(row)),
  pt2 AS (SELECT sum(l1.v * (SELECT count(*) FROM l2 WHERE l2.v = l1.v)) AS v FROM l1)
SELECT pt1.v AS 'Star 1', pt2.v AS 'Star 2'
FROM pt1, pt2;