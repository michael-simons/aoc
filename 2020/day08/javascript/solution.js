const fs = require('fs')

function run(instructions, ignoreJump) {
    const ran = new Set()

    let acc = 0
    let pos = 0
    while (pos < instructions.length) {
        if (ran.has(pos)) {
            return {acc: acc, aborted: true}
        }
        ran.add(pos)
        switch (instructions[pos][0]) {
            case 'acc': 
                acc += instructions[pos][1]
            case 'nop': 
                ++pos
                break
            case 'jmp':
                pos += pos == ignoreJump ? 1 : instructions[pos][1]
                break
        }
    }

    return {acc: acc, aborted: false}
}

const fileName = process.argv.length < 3 ? 'input.txt' : process.argv[2]
const instructions = fs.readFileSync(fileName, 'utf8')
    .split('\n')
    .filter(v => v.length !== 0)
    .map(line => line.split(' ').map((v,i) => i == 1 ? parseInt(v) : v))


console.log(`Star one ${run(instructions, -1).acc}`)

const jumps = instructions
    .map((instruction, i) => [i, instruction[0]])
    .filter(v => v[1] == 'jmp')
    .map(v => v[0])

for(i = 0; i < jumps.length; ++i) {
    result = run(instructions, jumps[i])
    if (!result.aborted) {
        console.log(`Star two ${result.acc}`)
        break
    }
}
