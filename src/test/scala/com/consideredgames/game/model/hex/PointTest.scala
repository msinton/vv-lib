package com.consideredgames.game.model.hex

import com.consideredgames.game.model.board.{BoardUtils, HexPosition}
import org.scalatest.{FunSuite, OptionValues}
import com.consideredgames.game.model.hex.HexType._
import com.consideredgames.game.model.hex.Side._

import scala.util.Random

/**
 * Created by matt on 26/03/15.
 */
class PointTest extends FunSuite with OptionValues {

  test("getRivers") {

    //            2
    //        3       4
    //            1
    //        5       7
    //            6

    val h1 = Hex(1, HexType.CLAY)
    val h2 = Hex(2, HexType.CLAY)
    val h3 = Hex(3, HexType.CLAY)
    val h4 = Hex(4, HexType.CLAY)
    val h5 = Hex(5, HexType.CLAY)
    val h6 = Hex(6, HexType.CLAY)
    val h7 = Hex(7, HexType.CLAY)

    val hexMap = collection.mutable.AnyRefMap.empty[HexPosition, Hex]
    hexMap.update(HexPosition(1, 1), h1)
    hexMap.update(HexPosition(1, 0), h2)
    hexMap.update(HexPosition(0, 0), h3)
    hexMap.update(HexPosition(2, 0), h4)
    hexMap.update(HexPosition(0, 1), h5)
    hexMap.update(HexPosition(1, 2), h6)
    hexMap.update(HexPosition(2, 1), h7)

    BoardUtils.connectHexes(hexMap)
    PointInitialiser.setupPoints(hexMap.values.iterator, new PointFactory())

    //points flowing from
    val p1 = h2.vertices(Vertex.E)
    val p2 = h2.vertices(Vertex.SE)
    val p3 = h1.vertices(Vertex.NW)
    val p4 = h3.vertices(Vertex.SE)

    //rivers
    RiverSegment(h2, Side.southEast).setFlowUsingFrom(p1)
    RiverSegment(h2, Side.south).setFlowUsingFrom(p2)
    RiverSegment(h1, Side.northWest).setFlowUsingFrom(p3)
    RiverSegment(h3, Side.south).setFlowUsingFrom(p4)

    assert(p1.getRivers.equals(Set(RiverSegment(Hex(2, CLAY), southEast))))
    assert(p2.getRivers.equals(Set(RiverSegment(Hex(2, CLAY), south), RiverSegment(Hex(2, CLAY), southEast))))
    assert(p3.getRivers.equals(Set(RiverSegment(Hex(1, CLAY), northWest), RiverSegment(Hex(2, CLAY), south))))
    assert(p4.getRivers.equals(Set(RiverSegment(Hex(3, CLAY), south), RiverSegment(Hex(1, CLAY), northWest))))
  }

  test("riversFlowingTo") {

    val h1 = Hex(1, HexType.CLAY)
    val h2 = Hex(2, HexType.CLAY)
    val h3 = Hex(3, HexType.CLAY)
    val h4 = Hex(4, HexType.CLAY)
    val h5 = Hex(5, HexType.CLAY)
    val h6 = Hex(6, HexType.CLAY)
    val h7 = Hex(7, HexType.CLAY)

    val hexMap = collection.mutable.AnyRefMap.empty[HexPosition, Hex]
    hexMap.update(HexPosition(1, 1), h1)
    hexMap.update(HexPosition(1, 0), h2)
    hexMap.update(HexPosition(0, 0), h3)
    hexMap.update(HexPosition(2, 0), h4)
    hexMap.update(HexPosition(0, 1), h5)
    hexMap.update(HexPosition(1, 2), h6)
    hexMap.update(HexPosition(2, 1), h7)

    BoardUtils.connectHexes(hexMap)
    PointInitialiser.setupPoints(hexMap.values.iterator, new PointFactory())

    //points flowing from
    val p1 = h2.vertices(Vertex.E)
    val p2 = h2.vertices(Vertex.SE)
    val p3 = h1.vertices(Vertex.NW)
    val p4 = h3.vertices(Vertex.SE)

    //rivers
    val r1 = RiverSegment(h2, Side.southEast)
    r1.setFlowUsingFrom(p1)
    val r2 = RiverSegment(h2, Side.south)
    r2.setFlowUsingFrom(p2)
    val r3 = RiverSegment(h1, Side.northWest)
    r3.setFlowUsingFrom(p3)
    val r4 = RiverSegment(h3, Side.south)
    r4.setFlowUsingFrom(p4)

    assert(p1.riversFlowingFrom().equals(Set(r1)))
    assert(p1.riversFlowingTo().isEmpty)
    assert(p2.riversFlowingFrom().equals(Set(r2)))
    assert(p2.riversFlowingTo().equals(Set(r1)))
    assert(p3.riversFlowingFrom().equals(Set(r3)))
    assert(p3.riversFlowingTo().equals(Set(r2)))
    assert(p4.riversFlowingFrom().equals(Set(r4)))
    assert(p4.riversFlowingTo().equals(Set(r3)))
  }

  test("divert point 2") {
    val h1 = Hex(1, HexType.CLAY)
    val h2 = Hex(2, HexType.CLAY)
    val h3 = Hex(3, HexType.CLAY)
    val h4 = Hex(4, HexType.CLAY)
    val h5 = Hex(5, HexType.CLAY)
    val h6 = Hex(6, HexType.CLAY)
    val h7 = Hex(7, HexType.CLAY)

    val hexMap = collection.mutable.AnyRefMap.empty[HexPosition, Hex]
    hexMap.update(HexPosition(1, 1), h1)
    hexMap.update(HexPosition(1, 0), h2)
    hexMap.update(HexPosition(0, 0), h3)
    hexMap.update(HexPosition(2, 0), h4)
    hexMap.update(HexPosition(0, 1), h5)
    hexMap.update(HexPosition(1, 2), h6)
    hexMap.update(HexPosition(2, 1), h7)

    BoardUtils.connectHexes(hexMap)
    PointInitialiser.setupPoints(hexMap.values.iterator, new PointFactory())

    //points flowing from
    val p1 = h2.vertices(Vertex.E)
    val p2 = h2.vertices(Vertex.SE)
    val p3 = h1.vertices(Vertex.NW)
    val p4 = h3.vertices(Vertex.SE)

    val riverNetwork = new RiverNetwork(new Random())
    riverNetwork.addRiver(h2, Side.southEast).value.setFlowUsingFrom(p1)
    riverNetwork.addRiver(h2, Side.south).value.setFlowUsingFrom(p2)
    riverNetwork.addRiver(h1, Side.northWest).value.setFlowUsingFrom(p3)
    riverNetwork.addRiver(h3, Side.south).value.setFlowUsingFrom(p4)

    assert(!p1.divertPossible())
    assert(p2.divertPossible())
    assert(p3.divertPossible())
    assert(p4.divertPossible())

    object randomStub extends Random {
      override def nextBoolean() = false
    }

    p2.divert(riverNetwork, randomStub)

    assert(h1.rivers.size === 4)
    assert(h1.rivers(Side.southEast).flow.value.from.getVertexWithRespectToHex(h1).value === Vertex.E)
    assert(h1.rivers(Side.south).flow.value.from.getVertexWithRespectToHex(h1).value === Vertex.SE)
    assert(h1.rivers(Side.southWest).flow.value.from.getVertexWithRespectToHex(h1).value === Vertex.SW)
    assert(h2.rivers.size === 1)
    assert(h2.rivers(Side.southEast).flow.value.from.getVertexWithRespectToHex(h2).value === Vertex.E)
    assert(h3.rivers.size === 1)
    assert(h3.rivers(Side.south).flow.value.from.getVertexWithRespectToHex(h3).value === Vertex.SE)
  }

  test("divert point 3") {
    val h1 = Hex(1, HexType.CLAY)
    val h2 = Hex(2, HexType.CLAY)
    val h3 = Hex(3, HexType.CLAY)
    val h4 = Hex(4, HexType.CLAY)
    val h5 = Hex(5, HexType.CLAY)
    val h6 = Hex(6, HexType.CLAY)
    val h7 = Hex(7, HexType.CLAY)

    val hexMap = collection.mutable.AnyRefMap.empty[HexPosition, Hex]
    hexMap.update(HexPosition(1, 1), h1)
    hexMap.update(HexPosition(1, 0), h2)
    hexMap.update(HexPosition(0, 0), h3)
    hexMap.update(HexPosition(2, 0), h4)
    hexMap.update(HexPosition(0, 1), h5)
    hexMap.update(HexPosition(1, 2), h6)
    hexMap.update(HexPosition(2, 1), h7)

    BoardUtils.connectHexes(hexMap)
    PointInitialiser.setupPoints(hexMap.values.iterator, new PointFactory())

    //points flowing from
    val p1 = h2.vertices(Vertex.E)
    val p2 = h2.vertices(Vertex.SE)
    val p3 = h1.vertices(Vertex.NW)
    val p4 = h3.vertices(Vertex.SE)

    val riverNetwork = new RiverNetwork(new Random())
    riverNetwork.addRiver(h2, Side.southEast).value.setFlowUsingFrom(p1)
    riverNetwork.addRiver(h2, Side.south).value.setFlowUsingFrom(p2)
    riverNetwork.addRiver(h1, Side.northWest).value.setFlowUsingFrom(p3)
    riverNetwork.addRiver(h3, Side.south).value.setFlowUsingFrom(p4)

    object randomStub extends Random {
      override def nextBoolean() = false
    }

    p3.divert(riverNetwork, randomStub)

    assert(h1.rivers.size === 1)
    assert(h1.rivers(Side.north).flow.value.from.getVertexWithRespectToHex(h1).value === Vertex.NE)
    assert(h2.rivers.size === 3)
    assert(h2.rivers(Side.southEast).flow.value.from.getVertexWithRespectToHex(h2).value === Vertex.E)
    assert(h2.rivers(Side.south).flow.value.from.getVertexWithRespectToHex(h2).value === Vertex.SE)
    assert(h2.rivers(Side.southWest).flow.value.from.getVertexWithRespectToHex(h2).value === Vertex.SW)
    assert(h3.rivers.size === 1)
    assert(h3.rivers(Side.northEast).flow.value.from.getVertexWithRespectToHex(h3).value === Vertex.E)
    assert(h4.rivers.size === 1)
  }

  test("divert point 4") {
    val h1 = Hex(1, HexType.CLAY)
    val h2 = Hex(2, HexType.CLAY)
    val h3 = Hex(3, HexType.CLAY)
    val h4 = Hex(4, HexType.CLAY)
    val h5 = Hex(5, HexType.CLAY)
    val h6 = Hex(6, HexType.CLAY)
    val h7 = Hex(7, HexType.CLAY)

    val hexMap = collection.mutable.AnyRefMap.empty[HexPosition, Hex]
    hexMap.update(HexPosition(1, 1), h1)
    hexMap.update(HexPosition(1, 0), h2)
    hexMap.update(HexPosition(0, 0), h3)
    hexMap.update(HexPosition(2, 0), h4)
    hexMap.update(HexPosition(0, 1), h5)
    hexMap.update(HexPosition(1, 2), h6)
    hexMap.update(HexPosition(2, 1), h7)

    BoardUtils.connectHexes(hexMap)
    PointInitialiser.setupPoints(hexMap.values.iterator, new PointFactory())

    //points flowing from
    val p1 = h2.vertices(Vertex.E)
    val p2 = h2.vertices(Vertex.SE)
    val p3 = h1.vertices(Vertex.NW)
    val p4 = h3.vertices(Vertex.SE)

    val riverNetwork = new RiverNetwork(new Random())
    riverNetwork.addRiver(h2, Side.southEast).value.setFlowUsingFrom(p1)
    riverNetwork.addRiver(h2, Side.south).value.setFlowUsingFrom(p2)
    riverNetwork.addRiver(h1, Side.northWest).value.setFlowUsingFrom(p3)
    riverNetwork.addRiver(h3, Side.south).value.setFlowUsingFrom(p4)

    object randomStub extends Random {
      override def nextBoolean() = false
    }

    p4.divert(riverNetwork, randomStub)

    assert(h2.rivers.size === 2)
    assert(h2.rivers(Side.southEast).flow.value.from.getVertexWithRespectToHex(h2).value === Vertex.E)
    assert(h2.rivers(Side.south).flow.value.from.getVertexWithRespectToHex(h2).value === Vertex.SE)
    assert(h3.rivers.size === 1)
    assert(h3.rivers(Side.southEast).flow.value.from.getVertexWithRespectToHex(h3).value === Vertex.E)
    assert(h5.rivers.size === 2)
    assert(h5.rivers(Side.northEast).flow.value.from.getVertexWithRespectToHex(h5).value === Vertex.NE)
    assert(h5.rivers(Side.southEast).flow.value.from.getVertexWithRespectToHex(h5).value === Vertex.E)
  }

  test("divert setup 2(network branch from point 2), point 5") {
    val h1 = Hex(1, HexType.CLAY)
    val h2 = Hex(2, HexType.CLAY)
    val h3 = Hex(3, HexType.CLAY)
    val h4 = Hex(4, HexType.CLAY)
    val h5 = Hex(5, HexType.CLAY)
    val h6 = Hex(6, HexType.CLAY)
    val h7 = Hex(7, HexType.CLAY)

    val hexMap = collection.mutable.AnyRefMap.empty[HexPosition, Hex]
    hexMap.update(HexPosition(1, 1), h1)
    hexMap.update(HexPosition(1, 0), h2)
    hexMap.update(HexPosition(0, 0), h3)
    hexMap.update(HexPosition(2, 0), h4)
    hexMap.update(HexPosition(0, 1), h5)
    hexMap.update(HexPosition(1, 2), h6)
    hexMap.update(HexPosition(2, 1), h7)

    BoardUtils.connectHexes(hexMap)
    PointInitialiser.setupPoints(hexMap.values.iterator, new PointFactory())

    //points flowing from
    val p1 = h2.vertices(Vertex.E)
    val p2 = h2.vertices(Vertex.SE)
    val p3 = h1.vertices(Vertex.NW)
    val p4 = h3.vertices(Vertex.SE)
    val p5 = h1.vertices(Vertex.E)

    val riverNetwork = new RiverNetwork(new Random())
    riverNetwork.addRiver(h2, Side.southEast).value.setFlowUsingFrom(p1)
    riverNetwork.addRiver(h2, Side.south).value.setFlowUsingFrom(p2)
    riverNetwork.addRiver(h1, Side.northWest).value.setFlowUsingFrom(p3)
    riverNetwork.addRiver(h3, Side.south).value.setFlowUsingFrom(p4)
    // branch
    riverNetwork.addRiver(h1, Side.northEast).value.setFlowUsingFrom(p2)
    riverNetwork.addRiver(h7, Side.north).value.setFlowUsingFrom(p5)

    assert(!p1.divertPossible())
    assert(!p2.divertPossible())
    assert(p3.divertPossible())
    assert(p4.divertPossible())
    assert(p5.divertPossible())

    object randomStub extends Random {
      override def nextBoolean() = true
    }

    p5.divert(riverNetwork, randomStub)

    assert(h1.rivers.size === 4)
    assert(h4.rivers.size === 2)
    assert(h7.rivers.size === 2)
    assert(h7.rivers(Side.northWest).flow.value.from.getVertexWithRespectToHex(h7).value === Vertex.NW)
    assert(h7.rivers(Side.southWest).flow.value.from.getVertexWithRespectToHex(h7).value === Vertex.W)
    assert(h6.rivers.size === 1)
  }

  test("divert setup 2(network branch from point 2, flows to point 2), point 5") {
    val h1 = Hex(1, HexType.CLAY)
    val h2 = Hex(2, HexType.CLAY)
    val h3 = Hex(3, HexType.CLAY)
    val h4 = Hex(4, HexType.CLAY)
    val h5 = Hex(5, HexType.CLAY)
    val h6 = Hex(6, HexType.CLAY)
    val h7 = Hex(7, HexType.CLAY)

    val hexMap = collection.mutable.AnyRefMap.empty[HexPosition, Hex]
    hexMap.update(HexPosition(1, 1), h1)
    hexMap.update(HexPosition(1, 0), h2)
    hexMap.update(HexPosition(0, 0), h3)
    hexMap.update(HexPosition(2, 0), h4)
    hexMap.update(HexPosition(0, 1), h5)
    hexMap.update(HexPosition(1, 2), h6)
    hexMap.update(HexPosition(2, 1), h7)

    BoardUtils.connectHexes(hexMap)
    PointInitialiser.setupPoints(hexMap.values.iterator, new PointFactory())

    //points flowing from
    val p1 = h2.vertices(Vertex.E)
    val p2 = h2.vertices(Vertex.SE)
    val p3 = h1.vertices(Vertex.NW)
    val p4 = h3.vertices(Vertex.SE)
    val p5 = h1.vertices(Vertex.E)
    val p6 = h4.vertices(Vertex.SE)

    val riverNetwork = new RiverNetwork(new Random())
    riverNetwork.addRiver(h2, Side.southEast).value.setFlowUsingFrom(p1)
    riverNetwork.addRiver(h2, Side.south).value.setFlowUsingFrom(p2)
    riverNetwork.addRiver(h1, Side.northWest).value.setFlowUsingFrom(p3)
    riverNetwork.addRiver(h3, Side.south).value.setFlowUsingFrom(p4)
    // branch
    riverNetwork.addRiver(h1, Side.northEast).value.setFlowUsingFrom(p5)
    riverNetwork.addRiver(h7, Side.north).value.setFlowUsingFrom(p6)

    assert(!p1.divertPossible())
    assert(!p2.divertPossible())
    assert(p3.divertPossible())
    assert(p4.divertPossible())
    assert(p5.divertPossible())
    assert(!p6.divertPossible())

    object randomStub extends Random {
      override def nextBoolean() = false
    }

    p5.divert(riverNetwork, randomStub)

    assert(h1.rivers.size === 5)
    assert(h2.rivers.size === 2)
    assert(h5.rivers.size === 2)
    assert(h6.rivers.size === 1)
    assert(h7.rivers.size === 2)
    assert(h4.rivers.size === 2)
  }

}