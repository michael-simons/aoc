// Part 1
function computeChecksum(lines: string[]): number {
    const groupChars: (input: string) => number[] = function (input: string): number[] {

        const groups = new Map<string, number>()
        for (const char of input) {
            const cnt = groups.get(char) ?? 0
            groups.set(char, cnt + 1)
        }

        return [...new Set(groups.values())]
            .filter((v: number) => v === 2 || v === 3)
    }

    return lines.map(groupChars).reduce((acc, x) => {
        acc[0] += x.includes(2) ? 1 : 0
        acc[1] += x.includes(3) ? 1 : 0
        return acc
    }, [0, 0]).reduce((acc, v) => acc * v, 1)
}

// Part 2
function computeCommonId(lines: string[]): string | undefined {
    for (const outer of lines) {
        for (const inner of lines) {
            if (outer === inner) {
                continue
            }
            let numberOfDifferences = 0
            let lastDifferentIndex = 0
            const chars = [...outer]
            chars.forEach((char, i) => {
                if (inner.charAt(i) !== char) {
                    ++numberOfDifferences
                    lastDifferentIndex = i
                }
            })
            if (numberOfDifferences === 1) {
                return outer.slice(0, lastDifferentIndex) + outer.slice(lastDifferentIndex + 1)
            }
        }
    }
}

import * as fs from 'fs'

const fileName = process.argv.length < 3 ? 'input.txt' : process.argv[2]
const lines = fs.readFileSync(fileName, 'utf8')
    .split('\n')
    .filter((v: string) => v.length !== 0)

const checksum = computeChecksum(lines)
const commonId = computeCommonId(lines)

console.log(`Checksum ${checksum}`)
console.log(`Common Id ${commonId}`)
