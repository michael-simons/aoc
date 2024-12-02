WITH words AS (
  SELECT MAP {'one': 1, 'two':2, 'three':3, 'four':4, 'five':5,  'six':6, 'seven':7, 'eight':8, 'nine':9} AS v
),
regex AS (
  SELECT -- Generate a regex from the words, each word becoming a capturing group, aggregating it to one string, with the | regex op,
         -- adding a group for actual numbers
         list_aggregate(list_transform(map_keys(words.v), k -> '(' || k || ')'), 'string_agg', '|') || '|(\d)'          AS v1,
         -- DuckDB uses RE2, which can't do lookahead regex. Therefor I'm reversing each word, and build with them a similar regex as above
         list_aggregate(list_transform(map_keys(words.v), k -> '(' || reverse(k) || ')'), 'string_agg', '|') || '|(\d)' AS v2
  FROM words
),
digits AS (
  SELECT -- Part one, get only the numbers from the input, easy
         list_filter(string_split(column0, ''), x -> TRY_CAST(x AS INTEGER) IS NOT NULL)      AS v1,
         -- Part two: Search for number words from the start
         list_transform(regexp_extract_all(column0, regex.v1), x -> ifnull(words.v[x][1], x::INTEGER)) AS v2,
         -- and search for number words from the end, see above, reversing the input string,
         -- using the reversed regex and also reversing the returned list
         list_transform(regexp_extract_all(reverse(column0), regex.v2), x -> ifnull(words.v[reverse(x)][1], x::INTEGER)) AS v3
  FROM words, regex, read_csv_auto('/dev/stdin', header=false)
),
numbers AS (
  SELECT v1[1] || v1[len(v1)] AS v1,
         v2[1] || v3[1]       AS v2
  FROM digits
)
SELECT sum(v1::integer) AS 'Star 1',
       sum(v2::integer) AS 'Star 2'
FROM numbers;
