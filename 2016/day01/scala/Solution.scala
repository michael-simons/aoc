#!/usr/bin/env scala

object Solution extends App {

  val input = io.Source.fromFile("input.txt")
  val instructions = input.getLines().flatMap( _.split(",")).map(_.trim).toSeq
  input.close

  case class Position(cd: String, x: Int, y: Int) {
    def distanceTo(other: Position) = (x - other.x).abs + (y - other.y).abs
  }

  def nextPosition(b: Position, instruction: String): Position = {
    val dir = instruction.substring(0, 1)
    val blocks = instruction.substring(1).toInt
    (b.cd, dir) match {
      case ("N", "R") | ("S", "L") => Position("W", b.x + blocks, b.y)
      case ("N", "L") | ("S", "R") => Position("O", b.x - blocks, b.y)
      case ("W", "R") | ("O", "L") => Position("S", b.x         , b.y - blocks)
      case ("W", "L") | ("O", "R") => Position("N", b.x         , b.y + blocks)
      case _ => b
    }
  }

  val start = Position("N",0,0)
  val numblocksTillEnd = instructions.foldLeft(start) { nextPosition }.distanceTo(start)
  println(s"Star one $numblocksTillEnd")
  
  def findFirstRevisitedPosition(
    instruction: Seq[String], 
    current: Position = start, 
    visitedPositions: Seq[Position] = Seq()
  ): Position = instruction match {
    case Nil => current
    case head :: tail => {
      val target = nextPosition(current, head)
      var nextPositions = Seq[Position]()
      var step = if (target.x >= current.x) 1 else -1
      nextPositions = nextPositions ++ (current.x until target.x by step).map(Position("_", _, current.y))
      step = if (target.y >= current.y) 1 else -1
      nextPositions = nextPositions ++ (current.y until target.y by step).map(Position("_", current.x, _))
      val revisitedPositions = nextPositions.filter(visitedPositions.contains(_))
      if (revisitedPositions.isEmpty) {
        findFirstRevisitedPosition(tail, target, visitedPositions ++ nextPositions)
      } else {
        revisitedPositions.head
      }
    }
  }

  val numBlocksTillFirstRevisit = findFirstRevisitedPosition(instructions).distanceTo(start)
  println(s"Star two $numBlocksTillFirstRevisit")
}
