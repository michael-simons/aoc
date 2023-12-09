WITH RECURSIVE input AS (
  SELECT row_number() OVER () AS rn, column0, FROM read_csv_auto('/dev/stdin')
),
histories AS (
  SELECT rn, unnest(list_transform(string_split(input.column0, ' '), x -> x::bigint)) AS v 
  FROM input
),
all_values AS ( -- Produce the seuences until we hit a sequence being only zeros
  SELECT rn,     1 AS g, v FROM histories
  UNION ALL
  SELECT rn, g + 1 AS g, 
         -- New value as diff between last and lag partitioned by origial row num
         v - (lag(v) OVER (PARTITION BY rn)) AS next_v
  FROM all_values a1
  -- Recursive end
  WHERE NOT 0 = ALL (SELECT v FROM all_values AS a2 WHERE a2.rn = a1.rn )
  -- Not interested in producing negative values (qualifying the window)
  QUALIFY lag(v) OVER (PARTITION BY rn) IS NOT NULL
),
sequences AS (-- Aggregate the tons of rows, also count the number of sequences per original row
  SELECT rn, g, list(v) AS sequence, 
         count() OVER (PARTITION by rn) AS num_sequences
  FROM all_values 
  GROUP BY rn, g
),
part1 AS (
  SELECT sum(sequence[-1:-:1][1]) AS v
  FROM sequences
),
backward_diff As (
  SELECT rn, g, sequence[1] AS v
  FROM sequences s 
  WHERE g = num_sequences
  UNION ALL
  SELECT s.rn, s.g, s.sequence[1] - bd.v as v 
  FROM backward_diff bd JOIN sequences s ON s.rn = bd.rn AND s.g = bd.g - 1
),
part2 AS (
  SELECT sum(v) AS v
  FROM backward_diff 
  WHERE g = 1
)
SELECT part1.v AS 'Star 1',
       part2.v AS 'Star 2'
FROM part1, part2;
