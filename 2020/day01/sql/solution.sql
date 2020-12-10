# Needed on MySQL
# SET GLOBAL local_infile = true;

DROP TABLE IF EXISTS input;
CREATE TABLE input(value INT);

LOAD DATA local INFILE 'input.txt' INTO TABLE input;

SELECT DISTINCT i1.value * i2.value  AS starOne
FROM input i1 CROSS JOIN input i2
WHERE i1.value + i2.value = 2020;

SELECT DISTINCT i1.value * i2.value * i3.value AS starTwo
FROM input i1 CROSS JOIN input i2 CROSS JOIN input i3
WHERE i1.value + i2.value + i3.value = 2020;
