package main

import (
	"bufio"
	"fmt"
	"log"
	"math"
	"os"
	"sort"
	"strconv"
	"strings"
)

type Block struct {
	low  uint
	high uint
}

// Slice type of blocks
type Blocks []Block

// Set of methods for sort.Interface
// Slice of blocks can be sorted by the low value of a block
func (a Blocks) Len() int           { return len(a) }
func (a Blocks) Swap(i, j int)      { a[i], a[j] = a[j], a[i] }
func (a Blocks) Less(i, j int) bool { return a[i].low < a[j].low }

func main() {

	args := os.Args[1:]

	var fileName string
	if len(args) == 0 {
		fileName = "input.txt"
	} else {
		fileName = args[0]
	}

	// Multiple return types, second is an error
	// that might occurred while reading the file
	file, err := os.Open(fileName)
	if err != nil {
		log.Fatal(err)
	}
	defer file.Close()

	// Read stuff line by line
	var r Blocks
	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		line := strings.Split(scanner.Text(), "-")
		// Use _ to ignore the second (error) component of the string parsing
		low, _ := strconv.ParseUint(line[0], 10, 32)
		high, _ := strconv.ParseUint(line[1], 10, 32)
		r = append(r, Block{low: uint(low), high: uint(high)})
	}

	if err := scanner.Err(); err != nil {
		log.Fatal(err)
	}

	sort.Sort(r)

	var conflatedBlocks Blocks
	for index := 0; index < len(r); index++ {

		// Struct is copied by value
		conflatedBlock := r[index]

		for index+1 < len(r) && r[index+1].low <= conflatedBlock.high+1 {
			index++
			if r[index].high > conflatedBlock.high {
				conflatedBlock.high = r[index].high
			}
		}
		conflatedBlocks = append(conflatedBlocks, conflatedBlock)
	}

	maximumAdress := uint(math.MaxUint32 + 1)

	numberOfAllowedIps := uint(0)
	low := uint(0)
	// Ignore the index of the range
	for _, block := range conflatedBlocks {
		numberOfAllowedIps += (block.low - low)
		low = block.high + 1
	}
	numberOfAllowedIps += maximumAdress - low

	fmt.Println("Lowest value IP not blocked:", conflatedBlocks[0].high+1)
	fmt.Println(numberOfAllowedIps, "of allowed IPs")
}
