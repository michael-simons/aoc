WITH remapping AS (
  SELECT MAP {'A':'a', 'K':'b', 'Q':'c', 'J':'d', 'T':'e', '9':'f', '8':'g', '7':'h', '6':'i', '5':'j', '4':'l', '3':'m', '2':'n'} AS o1,
         MAP {'A':'a', 'K':'b', 'Q':'c', 'J':'x', 'T':'e', '9':'f', '8':'g', '7':'h', '6':'i', '5':'j', '4':'l', '3':'m', '2':'n'} AS o2
),
input0 AS (
  SELECT column1::integer                                                        AS bid,
         string_split(column0, '')                                               AS cards,
         list_aggregate(list_transform(cards, c -> r.o1[c][1]),'string_agg', '') AS order_part1,
         list_aggregate(list_transform(cards, c -> r.o2[c][1]),'string_agg', '') AS order_part2,
  FROM read_csv('/dev/stdin', auto_detect=True, delim=' '), remapping r
),
remapped AS ( -- Remaps the card names to values that sort appropriately, also generates new decks
  SELECT bid, order_part1, order_part2,
         [{name: 'Star 1', cards: cards}] + [{name: 'Star 2', cards: list_transform(cards, x -> CASE WHEN x = 'J' THEN k ELSE x END)} FOR k IN map_keys(r.o1)] AS part,
  FROM input0, remapping r
),
decks AS ( -- Unnests the the lists of cards for each part, joining them together for a simple, groupable deck
  SELECT bid, games.unnest.name as part, order_part1, order_part2,
         list_aggregate(games.unnest.cards, 'string_agg', '') as deck,
         games.unnest.cards AS cards
  FROM remapped, LATERAL unnest(remapped.part) games
),
decks_and_counts AS ( -- Counts the different cards
    SELECT bid, part, order_part1, order_part2, deck, count(*) AS counts
    FROM decks, LATERAL unnest(decks.cards) card
    GROUP BY bid, part, order_part1, order_part2, deck, card
),
aggregated_counts AS ( -- Aggregates all the counts in a list, undoes the exploded cardinality
    SELECT bid, part, order_part1, order_part2, deck,
           list_reverse_sort(list(greatest(counts / cardinality(r.o1), counts % cardinality(r.o1)))) AS counts
    FROM decks_and_counts, remapping r
    GROUP BY ALL
),
strengths AS ( -- Computes the strength based on the counts, grouped by part
  SELECT bid, order_part1, order_part2, part,
         max(CASE WHEN len(counts) = 1                   THEN 7
                  WHEN len(counts) = 2 AND counts[1] = 4 THEN 6
                  WHEN len(counts) = 2 AND counts[1] = 3 THEN 5
                  WHEN len(counts) = 3 AND counts[1] = 3 THEN 4
                  WHEN len(counts) = 3 AND counts[1] = 2 THEN 3
                  WHEN len(counts) = 4                   THEN 2
                  ELSE                                        1 END) AS strength
  FROM aggregated_counts
  GROUP BY ALL
),
ranks AS (
  SELECT part,
         bid * rank() OVER (
           PARTITION BY part
           ORDER BY strength ASC,
                    CASE WHEN part = 'Star 1' THEN order_part1 ELSE order_part2 END DESC
         ) AS winning,
  from strengths
)
SELECT part, sum(winning) AS winnings
FROM ranks
GROUP BY ALL
ORDER BY ALL;
