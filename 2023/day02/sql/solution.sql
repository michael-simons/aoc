-- First create a temporary table "games", we'are not gonna solve this in one statement
CREATE OR REPLACE TABLE games AS 
  WITH raw_games AS (
    SELECT -- Extract the game number from the first column, turn it into a numeric ID
           trim(substr(split_part(column0,':', 1), 5))::integer AS id,
           -- Select all columns (the games, replacing the "game x" from the first)
           * replace(split_part(column0,':', 2 ) as column0)
    FROM -- Filling up missing columns while reading CSV with nulls
         read_csv('/dev/stdin', delim=';', null_padding=TRUE, AUTO_DETECT=TRUE)
  ),
  -- Sort the rgb values
  sorted_games AS (
    SELECT id, 
           -- Extract r, g, b values as struct
           regexp_extract(
             -- Aggregate it into one string again
             list_aggregate(
               -- Sort that list, so that is always blue, green, red
               list_sort(
                 -- Flip value and label, so that it becomes label and value
                 list_transform(
                   -- Split the games on the ,
                   string_split( 
                     -- This selects all columns, except the ID colum and applies all the functions around it to all colums
                     columns(* exclude(id)), ','
                   ),              
                   x -> split_part(trim(x),' ', 2 )|| ' ' || split_part(trim(x),' ', 1)
                 )
               ), 
               'string_agg', ', '
             ), 
             -- Now, "AS v" creates multiple columns named v. When addressiong them, they start at v, v:1, etc...
             '(?:blue (\d+))?(?:(?:, )?green (\d+))?(?:(?:, )?red (\d+))?', ['b', 'g', 'r']) AS v 
     FROM raw_games
   ),   
   -- Turn the struct with char or empty values into struct varchar: integer
   rgb_values AS (
     SELECT id, {
        -- The columns expression supports values cards, here all columns that start with v
       'r': ifnull(TRY_CAST(struct_extract(columns('v.*'), 'r') AS integer), 0),
       'g': ifnull(TRY_CAST(struct_extract(columns('v.*'), 'g') AS integer), 0),
       'b': ifnull(TRY_CAST(struct_extract(columns('v.*'), 'b') AS integer), 0)
     } AS rgb FROM sorted_games
   )
  SELECT * from rgb_values;

-- Star 1 is quite easy
SELECT sum(id) AS 'Star 1'
FROM games
WHERE struct_extract(columns('rgb.*'), 'r') <= 12
  AND struct_extract(columns('rgb.*'), 'g') <= 13
  AND struct_extract(columns('rgb.*'), 'b') <= 14;
  
-- For Star 2 we need to get the greatest values of an unknown number of columns per row…
-- I just looked up the column names in the data dictionary and created the select.

.header off
.mode column
.output .hlp.sql
WITH col_list AS (
  SELECT list(column_name) AS v FROM information_schema.columns WHERE column_name <> 'id'
),
cols as (
 SELECT 'greatest(' || list_aggregate(list_transform(v, c -> '"' || c || '".r'), 'string_agg', ',') || ')' as r,
        'greatest(' || list_aggregate(list_transform(v, c -> '"' || c || '".g'), 'string_agg', ',') || ')' as g,
        'greatest(' || list_aggregate(list_transform(v, c -> '"' || c || '".b'), 'string_agg', ',') || ')' as b,
 FROM col_list  
)
SELECT 'SELECT sum(' || r || ' * ' || g || ' * ' || b || ') AS ''Star 2'' FROM games' FROM cols;

.header on
.mode duckbox
.output
.read .hlp.sql
