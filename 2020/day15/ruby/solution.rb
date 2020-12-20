#!/usr/bin/env ruby

def solve(input, target)
  spoken = Array.new(target)

  last_number = nil
  input.each_with_index do |n, i|
    spoken[n] = [i + 1]
    last_number = n
  end

  (input.length+1..target).each do |i|
    unless spoken[last_number].length > 1 then
      last_number = 0
    else
      old_last = last_number
      last_number = spoken[old_last][1] - spoken[old_last][0]
      spoken[old_last].shift
    end

    spoken[last_number] = [] unless spoken[last_number]
    spoken[last_number] << i
  end

  last_number
end

input = File.read(ARGV.length == 1 ? ARGV[0] : "input.txt").split(",").map { |v| v.strip.to_i }

starOne = solve(input, 2020)
puts "Star one #{starOne}"

starTwo = solve(input, 30000000)
puts "Star two #{starTwo}"
