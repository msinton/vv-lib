package com.consideredgames.game.model.hex

import java.util

import com.consideredgames.game.model.animals.AnimalManager
import com.consideredgames.game.model.person.Person

import scala.collection._

/**
 * Created by Matt.Sinton-Hewitt on 09/03/2015.
 */
case class Hex protected(id: Int) extends Comparable[Hex] {

  import scala.collection.JavaConversions._

  var hiddenType: Option[HexType] = None

  var volcano: Boolean = false

  var lava: Boolean = false

  val neighbours: mutable.Map[Side, Hex] = new util.EnumMap[Side, Hex](classOf[Side])

  private var hexType_ : HexType = _

  /** The type that should replace a temporary type e.g. from flooding */
  private var maskedType = hexType

  private val rivers_ = new util.EnumMap[Side, RiverSegment](classOf[Side])

  private val bridges_ = new util.EnumMap[Side, Bridge](classOf[Side])

  private val boats_ = new util.EnumMap[Side, Boat](classOf[Side])

  val vertices: mutable.Map[Vertex, Point] = new util.EnumMap[Vertex, Point](classOf[Vertex])

  var person: Option[Person] = None

  var animalManager: Option[AnimalManager] = None

  var wall: Option[Wall] = None

  def hexType = hexType_

  def boats: mutable.Map[Side, Boat] = boats_

  def bridges: mutable.Map[Side, Bridge] = bridges_

  def rivers: mutable.Map[Side, RiverSegment] = rivers_

  def add(side: Side, bordersHex: BordersHex): Boolean = {
    bordersHex match {
      case boat: Boat => boats_.put(side, boat); true
      case r: RiverSegment => rivers_.put(side, r); true
      case bridge: Bridge => bridges_.put(side, bridge); true
      case _ => false
    }
  }

  def remove(side: Side, bordersHex: BordersHex): Boolean = {

    bordersHex match {
      case boat: Boat => boats_.remove(side, boat)
      case r: RiverSegment => rivers_.remove(side, r)
      case bridge: Bridge => bridges_.remove(side, bridge)
    }
  }

  def removePerson(): Option[Person] = person flatMap { p => person = None; Option(p) }

  def getVertex(point: Point): Option[Vertex] = vertices.find { case (_, p) => p == point } map (_._1)

  def getSide(hex: Hex): Option[Side] = neighbours.find { case (_, h) => h == hex } map (_._1)

  def getRiverNeighbours = {
    var rivers = Set[RiverSegment]()
    for (point <- vertices.values) {
      rivers = rivers ++ point.getRivers.toSet
    }
    rivers
  }

  /**
   * Enables a sorted map of hexes to be sorted by type - for faster rendering.
   */
  override def compareTo(o: Hex): Int = hexType.compareTo(o.hexType)
  
  def floodHex(): Unit = {
    maskedType = hexType
    hexType_ = HexType.FLOODED
  }

  def removeFlood(): Unit = {
    hexType_ = maskedType
  }

  def neighboursAccessibleByFoot(): collection.Map[Side,Hex] = {
    neighbours.filter { case (side, hex) => rivers_.contains(side) && !bridges_.contains(side)}
  }
}

object Hex {

  def apply(id: Int, hexType: HexType) = {
    val h = new Hex(id)
    h.hexType_ = hexType
    h
  }
}