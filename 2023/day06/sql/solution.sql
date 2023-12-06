-- Column0 are the labels, column1 the values, which we split into a list of chars
WITH input0 AS (
  SELECT * replace(list_filter(string_split(column1, ' '), x -> x <> '') AS column1)
  FROM read_csv('/dev/stdin', auto_detect=True, delim=':')
),
-- We transform the  strings into integers for  part one, and add  one list item
-- with the strings joined together first than turned into a string. We also add
-- an artifical column indicating part 1 for  the splited values each and part 2
-- for the joined values. DuckDB can do list comprehensions (see part column).
input AS (
  SELECT column0,
         list_transform(column1, x -> x::bigint) + [list_aggregate(column1, 'string_agg', '')::bigint] AS column1,
         ['Star 1' FOR x IN column1] + ['Star 2'] AS part
  FROM input0
),
-- We pivot on the labels so that we  have time and distance side by side. Right
-- now each row has one value anyhow, so we can use that one as an aggregate.
pivoted AS (
  PIVOT (SELECT * from input) ON column0 USING any_value(column1)
),
-- Then we unnest everything into a proper "table", finally
races AS (
  SELECT unnest(part)     AS part,
         unnest(Distance) AS Distance,
         unnest(Time)     AS Time
  FROM pivoted
),
-- The values time,  distance and x (the  number of ms pushing  the button) made
-- quadratic equasions based on the hint that  remaining_time = t - x results in
-- distance d  = remaining_time * x,  leading to x^2 -  tx + d =  0. The rounded
-- solutions to  those are the  minimum and maximum  time values of  pushing the
-- button that lead to  a better result. If it is exactly the  same value as the
-- result, one  one must  subtract 1 from  the difference to  get the  number of
-- possibilities.
args AS (
  SELECT part, distance, time,
         -(-Time/2)                      AS v1,
         sqrt(pow(-Time/2,2) - Distance) AS v2
  FROM races
),
solutions AS (
  SELECT part, distance, time,
         ceil(v1 - v2)  AS min,
         floor(v1 + v2) AS max
  FROM args
)
SELECT part, product(
         CASE WHEN ((time - max) * max > distance) THEN max + 1
              ELSE max-1 end - min)::bigint AS margin
FROM solutions GROUP BY ALL;
