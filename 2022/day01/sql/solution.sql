WITH input AS (
  SELECT row_number() OVER () AS rn,
         column0::integer AS cal, count() OVER () as cnt
  FROM read_csv_auto('/dev/stdin')
),
blanks AS (
  SELECT lag(rn, 1, 0) OVER () as rnp, rn, lead(rn) OVER() as rnn
  FROM input WHERE cal IS NULL
),
elves AS (
  SELECT dense_rank() OVER (ORDER BY CASE WHEN i.rn >= cnt AND rnn IS NULL THEN i.rn ELSE b.rn END) AS elf,
         cal
  FROM input i, blanks b
  WHERE (i.rn > b.rnp)
    AND (i.rn < b.rn OR i.rn >= cnt AND rnn IS NULL)
  order by i.rn asc
),
totals AS (
  SELECT elf, sum(cal) AS cals
  FROM elves
  GROUP BY ALL
),
top3 AS (
  SELECT cals
  FROM totals
  QUALIFY rank() OVER (ORDER BY cals DESC) <=3
)
SELECT max(top3.cals) AS 'Star 1',
       sum(top3.cals) AS 'Star 2'
FROM top3;