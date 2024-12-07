CREATE OR REPLACE FUNCTION solutions(p_operators) AS TABLE
  WITH RECURSIVE solver(result, operands, last_result) AS (
    SELECT result, operands[2:], operands[1]
    FROM query_table('input')
    UNION ALL
    SELECT result, operands[2:],
           CASE operator
             WHEN '+' THEN last_result + operands[1] 
             WHEN '*' THEN last_result * operands[1] 
             ELSE cast(last_result || operands[1] AS bigint)
          END AS next_value
    FROM solver, unnest(p_operators) AS _(operator)
    WHERE length(operands) != 0
      AND next_value <= result
  )
  SELECT sum(DISTINCT result) FROM solver WHERE last_result = result;

WITH input AS (
  SELECT column0 AS result, 
         list_transform(string_split(trim(column1), ' '), v -> v::bigint) AS operands 
  FROM read_csv('/dev/stdin', delim=':', header=False)
) 
SELECT * from solutions(['*', '+']) AS t1(star1), solutions(['*', '+', '||']) AS t2(star2);
