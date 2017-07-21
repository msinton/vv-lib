package com.consideredgames.game.model.board

import com.consideredgames.game.model.hex.{Boat, Hex, RiverSegment, Side}

/**
 * Created by Matt.Sinton-Hewitt on 18/03/2015.
 */

case class BoardUtils(boardData: BoardData) {

  def isAvailableForBoat(hex: Hex, side: Side): Boolean = BoardUtils.isAvailableForBoat(hex, side)

  def isAvailableForBoat(river: RiverSegment): Boolean = {
    !boardData.boats.exists {
      case Boat(river.hexA, river.sideA) => true
      case b@Boat(_, _) if river.sideB.nonEmpty && river.hexB.nonEmpty && b.hexB == river.hexB && b.sideB == river.sideB => true
    }
  }

  def mapToGameElement(hex: Hex): Option[Hex] = {
    boardData.getHex(hex.id)
  }

  def mapToGameElement(river: RiverSegment): Option[RiverSegment] = {
    boardData.riverNetwork.getRivers.find(_ == river)
  }

}

object BoardUtils {

  /**
   * Sets hexA and hexB as neighbours to each other.
   *
   * @param hexA
   * @param sideA The side of hexA that hexB is connected to.
   * @param hexB
   */
  private def connectHexes(hexA: Hex, sideA: Side, hexB: Hex) {
    hexA.neighbours.put(sideA, hexB)
    hexB.neighbours.put(sideA.opposite, hexA)
  }

  def connectHexes(hexes: java.util.Map[HexPosition, Hex]): Unit = {
    connectHexes(collection.JavaConversions.mapAsScalaMap(hexes))
  }

  def connectHexes(hexes: collection.mutable.Map[HexPosition, Hex]): Unit = {

    // Starting at 0 - this makes all the difference in how the hexes relate
    // to one another. (N,NE..)
    for (entry <- hexes) {
      val column: Int = entry._1.column
      val row: Int = entry._1.row
      val hex: Hex = entry._2

      // N
      val north = hexes.get(HexPosition(column, row - 1))
      north foreach (connectHexes(hex, Side.north, _))
      // S is not required, as N will effectively do this.

      // only need to connect up every other column for the following sides
      if (column % 2 == 0) {
        val northEast = hexes.get(HexPosition(column + 1, row))
        northEast foreach (connectHexes(hex, Side.northEast, _))

        val southEast = hexes.get(HexPosition(column + 1, row + 1))
        southEast foreach (connectHexes(hex, Side.southEast, _))

        val southWest = hexes.get(HexPosition(column - 1, row + 1))
        southWest foreach (connectHexes(hex, Side.southWest, _))

        val northWest = hexes.get(HexPosition(column - 1, row))
        northWest foreach (connectHexes(hex, Side.northWest, _))
      }
    }
  }

  def isAvailableForBoat(hex: Hex, side: Side): Boolean = hex.boats.get(side).isEmpty
}