DROP TABLE IF EXISTS input;
CREATE TABLE input(value INT);

-- Data loading, might be different for your database.
-- Needed on MySQL
-- SET GLOBAL local_infile = true;

LOAD DATA local INFILE 'input.txt' INTO TABLE input;

-- Shared parts

CREATE OR REPLACE VIEW v_differences AS
WITH source AS  (
    SELECT 0 AS value
    UNION
    SELECT value FROM input
),
differences AS (
    SELECT value, ifnull(lead(value) OVER (ORDER BY value asc), value + 3) - value AS difference 
    FROM source
)
SELECT * FROM differences;


-- Part 1
WITH groups AS (
    SELECT difference, count(*) AS cnt
    FROM v_differences
    GROUP BY difference
) 
SELECT DISTINCT g1.cnt * g2.cnt AS starOne
FROM groups g1 CROSS JOIN groups g2 ON g1.difference <> g2.difference;

-- Part 2
WITH consecutive_1s AS (
    SELECT count(*) as length
    FROM (
        SELECT difference,
               (row_number() OVER (ORDER BY value) - row_number() over (PARTITION BY difference ORDER BY value)) as grp
         FROM v_differences
    ) hlp
    WHERE difference = 1
    GROUP BY grp
),
number_of_spots AS ( 
    SELECT count(*) AS cnt, length 
    FROM consecutive_1s 
    WHERE length > 1
    GROUP BY length
),
combinations AS (
    SELECT CASE length
        WHEN 2 THEN pow(2, cnt)
        WHEN 3 THEN pow(4, cnt)
        WHEN 4 THEN pow(7, cnt)
    END as value
    FROM number_of_spots
) 
SELECT round(exp(sum(ln(value)))) as starTwo
FROM combinations;
