DROP TABLE IF EXISTS input;
CREATE TABLE input(id SERIAL PRIMARY KEY, value VARCHAR(32));

-- Data loading, might be different for your database.
-- SET GLOBAL local_infile = true; -- Needed on MySQL
LOAD DATA local INFILE 'input.txt' INTO TABLE input(value);

-- Shared parts
CREATE OR REPLACE VIEW v_cmds AS
WITH source AS  (
    SELECT id,
           substr(value, 1, 1) AS dir,
           CAST(substr(value, 2) AS int) AS i
    FROM input
    ORDER BY id ASC
), cmds AS (
    SELECT id, dir, i,
           CASE dir
           WHEN 'N' THEN concat( 0,',', 1)
           WHEN 'E' THEN concat( 1,',', 0)
           WHEN 'S' THEN concat( 0,',',-1)
           WHEN 'W' THEN concat(-1,',', 0) END AS modifier
    FROM source
)
SELECT * FROM cmds;

-- Part 1
WITH recursive cmds AS (
    SELECT * FROM v_cmds
),
steps(id, x, y, curdir) AS (
    SELECT 0 AS id , 0 AS x, 0 AS y, 'E' AS curdir
    UNION
    SELECT cmds.id,
           steps.x +
                CASE cmds.dir
                WHEN 'L' THEN 0
                WHEN 'R' THEN 0
                WHEN 'F' THEN
                    CASE curdir
                    WHEN 'E' THEN i
                    WHEN 'W' THEN -i
                    ELSE 0 END
                ELSE i * CAST(substr(modifier, 1, instr(modifier, ',') -1 ) AS int)
                END,
           steps.y +
                CASE cmds.dir
                WHEN 'L' THEN 0
                WHEN 'R' THEN 0
                WHEN 'F' THEN
                    CASE curdir
                    WHEN 'N' THEN i
                    WHEN 'S' THEN -i
                    ELSE 0 END
                ELSE i * CAST(substr(modifier, instr(modifier, ',') +1 ) AS int)
                END,
           CASE cmds.dir
           WHEN 'L' THEN
                CASE curdir
                WHEN 'E' THEN CASE i WHEN 90 THEN 'N' WHEN 180 THEN 'W' ELSE 'S' END
                WHEN 'W' THEN CASE i WHEN 90 THEN 'S' WHEN 180 THEN 'E' ELSE 'N' END
                WHEN 'N' THEN CASE i WHEN 90 THEN 'W' WHEN 180 THEN 'S' ELSE 'E' END
                WHEN 'S' THEN CASE i WHEN 90 THEN 'E' WHEN 180 THEN 'N' ELSE 'W' END
                END
           WHEN 'R' THEN
                CASE curdir
                WHEN 'E' THEN CASE i WHEN 90 THEN 'S' WHEN 180 THEN 'W' ELSE 'N' END
                WHEN 'W' THEN CASE i WHEN 90 THEN 'N' WHEN 180 THEN 'E' ELSE 'S' END
                WHEN 'N' THEN CASE i WHEN 90 THEN 'E' WHEN 180 THEN 'S' ELSE 'W' END
                WHEN 'S' THEN CASE i WHEN 90 THEN 'W' WHEN 180 THEN 'N' ELSE 'E' END
                END
           ELSE curdir END
    FROM cmds
    JOIN steps ON steps.id + 1 = cmds.id
)
SELECT abs(x) + abs(y) AS starOne
FROM steps
ORDER BY id DESC LIMIT 1;

WITH recursive cmds AS (
    SELECT * FROM v_cmds
),
steps(id, x, y, wx, wy) AS (
    SELECT 0 AS id,
           0 AS x, 0 AS y,
           10 AS wx, 1 AS wy
    UNION
    SELECT cmds.id,
           steps.x +
                CASE cmds.dir
                WHEN 'F' THEN (wx - x) * i
                ELSE 0 END,
           steps.y +
                CASE cmds.dir
                WHEN 'F' THEN (wy - y) * i
                ELSE 0 END,
            CASE cmds.dir
            WHEN 'F' THEN wx + (wx -x)* i
            WHEN 'L' THEN
                CASE i WHEN   90 THEN -wy + y + x
                       WHEN  180 THEN -wx + 2*x
                       WHEN  270 THEN  wy - y + x END
            WHEN 'R' THEN
                CASE i WHEN   90 THEN  wy - y + x
                       WHEN  180 THEN -wx + 2*x
                       WHEN  270 THEN -wy + y + x END
            ELSE wx + i * CAST(substr(modifier, 1, instr(modifier, ',') -1 ) AS int)
            END,
            CASE cmds.dir
            WHEN 'F' THEN wy + (wy-y) * i
            WHEN 'L' THEN
               CASE i WHEN  90 THEN  wx - x + y
                      WHEN 180 THEN -wy + 2*y
                      WHEN 270 THEN -wx + x + y END
            WHEN 'R' THEN
               CASE i WHEN  90 THEN -wx + x + y
                      WHEN 180 THEN -wy + 2*y
                      WHEN 270 THEN  wx - x + y END
            ELSE wy + i * CAST(substr(modifier, instr(modifier, ',') + 1 ) AS int)
            END
    FROM cmds
    JOIN steps ON steps.id + 1 = cmds.id
)
SELECT abs(x) + abs(y) AS starTwo
FROM steps
ORDER BY id DESC LIMIT 1;
