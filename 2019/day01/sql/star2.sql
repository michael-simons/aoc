WITH RECURSIVE fuel_for_fuel(fuel) AS ( -- <1>
  SELECT mass/3-2 from modules -- <2>
  UNION ALL
  SELECT * FROM ( -- <3>
    SELECT fuel/3-2 AS fuel FROM fuel_for_fuel
  ) hlp WHERE fuel > 0
)
SELECT sum(fuel) from fuel_for_fuel;
