#!/usr/bin/env groovy

import groovy.transform.Immutable

@Immutable class Nanobot {
  Integer x, y, z
  Integer range
  
  private def distanceTo(Nanobot other) {
    (x - other.x).abs() + (y - other.y).abs() + (z - other.z).abs()
  }
  
  def inRangeOf(Nanobot other) {
    distanceTo(other) <= range
  }
}

def pattern = /pos=<(-?\d+),(-?\d+),(-?\d+)>, r=(\d+)/

def file = new File(args.length < 1 ? 'input.txt' : args[0])

def bots = file.collect {line -> 
  def matcher = line =~ pattern
  def values = matcher[0][1..-1]
  return new Nanobot(values[0].toInteger(), values[1].toInteger(), values[2].toInteger(), values[3].toInteger())
}

def botWithMaximumRange = bots.max{a, b -> a.range.compareTo(b.range)}
def numberOfReachableBots = bots.count { botWithMaximumRange.inRangeOf(it)}

println "${numberOfReachableBots} reachable bots"
