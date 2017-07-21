package com.consideredgames.game.model.hex

import scala.collection.mutable.ListBuffer

object BordersHex {
  /**
   * Use of this method is to avoid duplicate boats etc.
   *
   * @param hex
   * @param side
   * @return
   */
  def listContainsBordersHexAtLocation(bordersHexes: Array[BordersHex], hex: Hex, side: Side): Boolean = {
    bordersHexes.exists(_.contains(hex, side))
  }

  def hexB(hexA: Hex, sideA: Side): Option[Hex] = {
    hexA.neighbours.get(sideA)
  }

  def sideB(hexA: Hex, hexB: Option[Hex]): Option[Side] = {
    hexB match {
      case Some(hex) => hex.getSide(hexA)
      case _ => None
    }
  }
}

abstract class BordersHex {

  addToHexes(sideA)

  def hexA: Hex

  def sideA: Side

  def hexB: Option[Hex] = BordersHex.hexB(hexA, sideA)

  def sideB: Option[Side] = BordersHex.sideB(hexA, hexB)

  protected def addToHexes(sideA: Side) {
    hexA.add(sideA, this)
    // add for hexB
    (hexB, sideB) match {
      case (Some(h), Some(s)) => h.add(s, this)
      case _ =>
    }
  }

  def removeFromHexes() = {
    hexA.remove(sideA, this)
    hexB foreach (_.remove(sideB.get, this))
  }

  def contains(hex: Hex, side: Side): Boolean = {
    ((hexA == hex) && (sideA == side)) || (hexB.contains(hex) && sideB.contains(side))
  }

  /**
   * If the point given is one of the river's points then returns its <i>other</i> point.
   */
  final def getOtherPoint(point: Point): Option[Point] = {

    val pointA = hexA.vertices.get(sideA.clockwiseVertex)
    val pointB = hexA.vertices.get(sideA.anticlockwiseVertex)

    if (pointA.contains(point)) {
      pointB
    }
    else if (pointB.contains(point)) {
      pointA
    }
    else {
      None
    }
  }

  /**
   * @return All the hexes which are neighbours of this BordersHexes, up to a maximum possible of four.
   */
  def getHexNeighbours = {
    val hexes = ListBuffer(hexA)
    hexB foreach (hexes.append(_))
    hexA.neighbours.get(sideA.clockwise) foreach (hexes.append(_))
    hexA.neighbours.get(sideA.anticlockwise) foreach (hexes.append(_))
    hexes
  }

}