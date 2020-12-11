class Solution {

    static class Grid {

        List<char[]> seats = []

        int starOne(int row, int col) {
            return [row - 1..row + 1, col - 1..col + 1]
            .combinations()
            .count {
                def (r, c) = it
                return !(r == row && c == col) && r >= 0 && c >= 0 && r < seats.size && c < seats[r].size && seats[r][c] == '#'
            }
        }

        int starTwo(int row, int col) {
            int occupiedSeats = 0
            def seatInView = {r, c ->
                if (seats[r][c] != '.') {
                    occupiedSeats += (seats[r][c] == '#' ? 1 : 0)
                    true
                }
            }

            for (int r = row - 1; r >= 0; --r) {
                if (seatInView(r, col)) {
                    break
                }
            }
            for (int r = row + 1; r < seats.size; ++r) {
                if (seatInView(r, col)) {
                    break
                }
            }
            for (int c = col - 1; c >= 0; --c) {
                if (seatInView(row, c)) {
                    break
                }
            }
            for (int c = col + 1; c < seats[row].size; ++c) {
                if (seatInView(row, c)) {
                    break
                }
            }
            for (int r = row - 1, c = col - 1; r >= 0 && c >= 0; --r, --c ) {
                if (seatInView(r, c)) {
                    break
                }
            }
            for (int r = row - 1, c = col + 1; r >= 0 && c < seats[r].size; --r, ++c ) {
                if (seatInView(r, c)) {
                    break
                }
            }
            for (int r = row + 1, c = col - 1; r < seats.size && c >= 0; ++r, --c ) {
                if (seatInView(r, c)) {
                    break
                }
            }
            for (int r = row + 1, c = col + 1; r < seats.size && c < seats[r].size; ++r, ++c ) {
                if (seatInView(r, c)) {
                    break
                }
            }
            return occupiedSeats
        }

        int numOccupied()  {
            return seats.flatten().count { it == '#' }
        }

        boolean iterate(int tolerance, Closure count) {
            List<char[]> newSeats = []

            seats.eachWithIndex { row, rowIndex ->
                newSeats[rowIndex] = []
                row.eachWithIndex { col, colIndex ->
                    int numOccupied = count(rowIndex, colIndex)

                    if (col == 'L' && numOccupied == 0) {
                        newSeats[rowIndex] << '#'
                    } else if (col == '#' && numOccupied >= tolerance) {
                        newSeats[rowIndex] << 'L'
                    } else {
                        newSeats[rowIndex] << col
                    }
                }
            }
            boolean changed = seats != newSeats
            seats = newSeats
            return changed
        }
    }

    static void main(String... args) {
        List<char[]> seats = []
        File.newInstance(args.length == 0 ? 'input.txt' : args[0]).eachLine { line, number ->
            seats << (line.split('') as List)
        }

        Grid grid = new Grid(seats: seats)
        while (grid.iterate(4, grid.&starOne)) { }
        println "Star one: ${grid.numOccupied()}"

        grid = new Grid(seats: seats)
        while (grid.iterate(5, grid.&starTwo)) { }
        println "Star two: ${grid.numOccupied()}"
    }

}
