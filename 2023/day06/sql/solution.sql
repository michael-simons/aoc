-- will be polished and explained soon
WITH input1 AS (
  SELECT * replace(list_filter(string_split(column1, ' '), x -> x <> '') AS column1)
  FROM read_csv('/dev/stdin', auto_detect=True, delim=':')  
),
input AS (
  SELECT column0,
         list_transform(column1, x -> x::bigint) + [list_aggregate(column1, 'string_agg', '')::bigint] AS column1,
         ['Star 1' FOR x IN column1] + ['Star 2'] AS part
  FROM input1
),
pivoted AS (pivot (select * from input) on column0 using any_value(column1)),
races as (select unnest(part) AS part, unnest(Distance) AS Distance, unnest(Time) AS Time from pivoted),
pq as (select part, distance, time, -(-Time/2) AS v1, sqrt(pow(-Time/2,2)-Distance) AS v2 from races),
goals as(select part, distance, time, floor((pq.v1 + pq.v2)) as max, ceil(pq.v1 - pq.v2) AS min from pq)
select part, product(case when ((time - max) * max > distance) then max + 1 else max-1 end-  min)::bigint AS margin
from goals group by part;