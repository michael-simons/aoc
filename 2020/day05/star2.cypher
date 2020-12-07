MATCH (s:Seat) WITH max(s.seatId) as maxSeatId
MATCH (s:Seat) 
WHERE s.seatId <> maxSeatId
  AND NOT EXISTS {
	MATCH (:Seat {seatId: s.seatId + 1})
} RETURN DISTINCT s.seatId + 1 AS freeSeatId;
