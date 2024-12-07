CREATE OR REPLACE FUNCTION solutions(p_operators) AS TABLE
  WITH RECURSIVE solver(result, idx, operands, op, last_result) AS (
    SELECT result, 1, operands, '', operands[1]
    FROM query_table('input')
    UNION ALL
    SELECT result, idx + 1 AS next_idx,
           operands, operators.value AS current_op,
           CASE current_op
             WHEN '+' THEN last_result + operands[next_idx] 
             WHEN '*' THEN last_result * operands[next_idx] 
             ELSE cast(last_result || operands[next_idx] AS bigint)
          END AS next_value
    FROM solver, unnest(p_operators) AS operators(value)
    WHERE next_idx <= length(operands)
     AND next_value <= result
  )
  SELECT sum(DISTINCT result) FROM solver WHERE last_result = result;

WITH input AS (
  SELECT column0 AS result, 
         list_transform(string_split(trim(column1), ' '), v -> v::bigint) AS operands 
  FROM read_csv('/dev/stdin', delim=':', header=False)
) 
SELECT * from solutions(['*', '+']) AS t1(star1), solutions(['*', '+', '||']) AS t2(star2);
