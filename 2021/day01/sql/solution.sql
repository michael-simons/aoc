DROP TABLE IF EXISTS input;
CREATE TABLE input(id SERIAL PRIMARY KEY, value INT);

-- Data loading, might be different for your database.
-- Needed on MySQL
-- SET GLOBAL local_infile = true;

-- LOAD DATA local INFILE 'test-input.txt' INTO TABLE input(value);
LOAD DATA local INFILE 'input.txt' INTO TABLE input(value);

WITH 
    diffs AS (
        SELECT value - coalesce(lag(value) over (order by id),value) AS value
        FROM input ORDER BY id
    ) 
SELECT count(*) AS 'part1' FROM diffs WHERE value > 0;

WITH 
    src AS (
        SELECT row_number() over(order by id) AS rn,
               value 
                + coalesce(lag(value,1) over (order by id),0)
                + coalesce(lag(value,2) over (order by id),0) AS value
        FROM input ORDER BY id
    ), 
    diffs AS (
        SELECT value - coalesce(lag(value) over (order by rn),value) AS value
        FROM src 
        WHERE rn >= 3
    )
SELECT count(*) AS 'part2' FROM diffs WHERE value > 0;
