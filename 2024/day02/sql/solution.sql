WITH recursive
  reports AS (
    SELECT row_number() OVER () as row, 
           list_transform(string_split(level, ' '), v -> v::int) as value 
    FROM read_csv('/dev/stdin',columns={level:varchar}, header=false)),
  hlp as (
    SELECT row, 1 AS idx, reports.value as value
    FROM reports
    UNION ALL
    SELECT row, idx + 1, list_filter(reports.value, (v, i) -> i != idx)
    FROM hlp JOIN reports using(row)
    WHERE hlp.idx <= length(reports.value)
  ),
  pairs AS (
    SELECT row, idx, list_zip(value[:-2], value[2:]) AS pair FROM hlp
  ),
  increases as (select row, idx, list_transform(pair, v -> v[2] - v[1]) AS value from pairs  where [1,2,3] @> value or [-1,-2,-3]@>value)
SELECT count(distinct row) filter (idx = 1) AS 'Star 1',
       count(distinct row) AS 'Star 2'
FROM increases;