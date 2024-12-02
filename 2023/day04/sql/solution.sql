-- The indicator RECURSIVE is required so that a CTE can be referenced inside itself again
WITH RECURSIVE input AS (
  -- just reading the input here and massing colum0 into a numeric id and the numbers
  SELECT substr(string_split(column0, ':')[1],5)::integer AS id,
         substr(column0, strpos(column0, ':') + 1)        AS column0,
         column1
  FROM read_csv('/dev/stdin', delim='|', auto_detect=True, header=false)
),
deck AS (
  -- Splitting the winning numbers and the numbers at hands into list, computing intersection
  -- and their size
  SELECT id,
         length(list_intersect(
           list_filter(string_split(column0, ' '), x -> x <> ''), 
           list_filter(string_split(column1, ' '), x -> x <> '')
         )) AS num_winning_cards
  FROM input
),
part1 AS (
  -- Just writing down the formula of doubling the points for each winning card
  SELECT sum(power(2, num_winning_cards-1)::integer) AS v FROM deck
),
final_deck AS ( 
  -- This is now the recursive query, we start with all entries of the deck
  SELECT * FROM deck
  UNION ALL
  -- And union them together with again by using the current state of affairs (final_deck)
  -- as driver to select again from the deck, unless there's no more entry in the deck 
  -- that has a higher value than any entry in the final deck so far
  SELECT deck.* 
  FROM final_deck, deck 
  WHERE deck.id BETWEEN final_deck.id + 1 AND final_deck.id + final_deck.num_winning_cards
),
part2 AS (
  SELECT count(*) AS v FROM final_deck
)
SELECT part1.v AS 'Star 1',
       part2.v AS 'Star 2'
FROM part1, part2;
