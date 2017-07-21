package com.consideredgames.game.model.hex

import scala.annotation.tailrec
import scala.util.Random

case class Point(id: Int) {

  import scala.collection.JavaConversions._

  /** Hexes with the vertex this point corresponds to */
  val hexes: collection.Map[Vertex, Hex] = new java.util.EnumMap[Vertex, Hex](classOf[Vertex])

  /**
   * A point can only have 3 hexes.
   * Once a hex has been added to a point this restricts the remaining positions to a set of 3(actually remaining 2) vertices.
   *
   * Performs checks to ensure the (vertex,hex) being added is valid - given that there are two possible configurations
   * for a point (SW,E,NW) or (SE,W,NE)
   */
  def setOneOfThreeHexes(vertex: Vertex, hex: Hex): Unit = {
    if (hexes.size < 3) {
      if (!hexes.containsKey(vertex) && (hexes.isEmpty
        || (Vertex.verticesSet_1.containsAll(hexes.keys) && Vertex.verticesSet_1.contains(vertex))
        || (Vertex.verticesSet_2.containsAll(hexes.keys) && Vertex.verticesSet_2.contains(vertex)))) {

        hexes.put(vertex, hex)
      }
    }
  }

  def getRivers = {
    var rivers = Set[RiverSegment]()

    for ((vertex, hex) <- hexes) {
      val sideClockwise = vertex.getClockwiseSide
      hex.rivers.get(sideClockwise).foreach(r => rivers = rivers + r)

      val sideAntiClockwise = vertex.getAnticlockwiseSide
      hex.rivers.get(sideAntiClockwise).foreach(r => rivers = rivers + r)
    }
    rivers
  }

  /**
   * @return The rivers which flow <b>to</b> this point
   */
  def riversFlowingTo() = getRivers.filter { river => river.flow.exists(_.to == this) }

  /**
   * @return The rivers which flow out <b>from</b> this point.
   */
  def riversFlowingFrom() = getRivers.filter { river => river.flow.exists(_.from == this) }

  def divertPossible() = riversFlowingTo().size == 1 && riversFlowingFrom().size == 1

  def getVertexWithRespectToHex(hex: Hex): Option[Vertex] = hexes.find(e => e._2 == hex) map (_._1)

  private final def getDirection(hex: Hex, side: Side): Option[Direction] = {
    if (hexes.containsValue(hex)) {
      if (getVertexWithRespectToHex(hex).exists(_.getClockwiseSide == side)) {
        Option(Direction.CLOCKWISE)
      } else if (getVertexWithRespectToHex(hex).exists(_.getAnticlockwiseSide == side)) {
        Option(Direction.ANTICLOCKWISE)
      } else {
        None
      }
    } else {
      None
    }
  }

  final def createNewBranch(hex: Hex, direction: Direction, limit: Int, riverNetwork: RiverNetwork): Unit = {

    if (limit - 1 > 0) {

      var side: Option[Side] = None
      var nextVertex: Option[Vertex] = None
      var nextSide: Option[Side] = None

      val vertex = getVertexWithRespectToHex(hex)
      if (direction == Direction.ANTICLOCKWISE) {
        side = vertex map (_.getAnticlockwiseSide)
        nextSide = side map (_.anticlockwise())
        nextVertex = vertex map (_.anticlockwise())
      } else {
        side = vertex map (_.getClockwiseSide)
        nextSide = side map (_.clockwise())
        nextVertex = vertex map (_.clockwise())
      }

      if (!side.exists(hex.rivers.contains)) {
        val nextPoint = nextVertex flatMap hex.vertices.get

        nextPoint foreach {
          p => {
            riverNetwork.addRiver(hex, side.get).foreach(_.setFlow(this, p))
            if (p.riversFlowingFrom().isEmpty) p.createNewBranch(hex, direction, limit - 1, riverNetwork)
          }
        }
      }
    }
  }

  @tailrec final def deleteBranch(riverNetwork: RiverNetwork): Unit = {
    val fromRivers = riversFlowingFrom()
    // if there is just one river flowing from this point and there is nothing flowing to here then delete.
    if (fromRivers.size == 1 && riversFlowingTo().isEmpty) {
      val river = fromRivers.head
      // This must come before calling delete - since we need riversFlowingTo to be empty
      riverNetwork.removeRiver(river)
      river.flow.get.to.deleteBranch(riverNetwork)
    }
  }

  def divert(riverNetwork: RiverNetwork, random: Random): Boolean = {

    val fromRivers = riversFlowingFrom()
    if (divertPossible()) {
      val fromRiver = fromRivers.head
      val sideHex = getFreeSideHex(random).get

      createNewBranch(sideHex._2, getDirection(sideHex._2, sideHex._1).get, 5, riverNetwork)

      // The initial river has to be deleted first, then we can use deleteBranch to do the rest.
      // This is because of the way deleteBranch works and the fact we have already created a new branch.
      val nextPointForRiverDeletion = fromRiver.getOtherPoint(this)
      nextPointForRiverDeletion foreach {
        riverNetwork.removeRiver(fromRiver)
        _.deleteBranch(riverNetwork)
      }
      return true
    }
    false
  }

  def getFreeSideHex(random: Random): Option[(Side, Hex)] = {

    var freeSideHexes: List[(Side, Hex)] = List()

    for ((vertex, hex) <- hexes) {

      val sideClockwise = vertex.getClockwiseSide
      if (hex.rivers.get(sideClockwise).isEmpty) {
        freeSideHexes = (sideClockwise, hex) :: freeSideHexes
      }
      val sideAntiClockwise = vertex.getAnticlockwiseSide
      if (hex.rivers.get(sideAntiClockwise).isEmpty) {
        freeSideHexes = (sideAntiClockwise, hex) :: freeSideHexes
      }
    }
    // randomly select the free-side-hex - they usually come in pairs. (This means you cant predict which way a diverted river will flow)
    if (random.nextBoolean()) Option(freeSideHexes.head) else Option(freeSideHexes.last)
  }
}