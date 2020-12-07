MATCH p=(:Start) - [:F|B*7] -> (r)
WITH reduce(
		hlp = {min: 0, max: 127}, 
		n IN relationships(p) | 
			CASE WHEN type(n) = 'F' 
				THEN {min: hlp.min, max: hlp.min + (hlp.max - hlp.min)/2} 
				ELSE {min: hlp.max - (hlp.max - hlp.min)/2 , max: hlp.max}
			END 
	).min AS rowNum, 
	r
MATCH p=(r) - [:L|R*3] -> (c)
WITH reduce(
		hlp = {min: 0, max: 7}, 
		n IN relationships(p) | 
			CASE WHEN type(n) = 'L' 
				THEN {min: hlp.min, max: hlp.min + (hlp.max - hlp.min)/2} 
				ELSE {min: hlp.max - (hlp.max - hlp.min)/2 , max: hlp.max}
			END 
	).min AS colNum,
	rowNum,
	last(nodes(p)) as seat
SET seat.seatId = rowNum*8+colNum 
RETURN max(seat.seatId) AS maxSeatId;
