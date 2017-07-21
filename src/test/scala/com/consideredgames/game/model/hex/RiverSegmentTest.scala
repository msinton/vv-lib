package com.consideredgames.game.model.hex

import com.consideredgames.game.model.board.{BoardUtils, HexPosition}
import org.scalatest.{FunSuite, OptionValues}

class RiverSegmentTest extends FunSuite with OptionValues {

  test("basics - just one hex") {
    val h = Hex(1, HexType.CLAY)
    val s = Side.north
    val r = RiverSegment(h, s)

    assert(!r.hasFlow)

    assert(r.getNeighbours(inflowDirection = true).isEmpty)
    assert(r.getNeighbours(inflowDirection = false).isEmpty)

    assert(!r.flowsTowards(h))

    assert(r.getHexNeighbours.size === 1)
    assert(r.getHexNeighbours.contains(h))

    assert(r.hexA === h)
    assert(r.sideA === s)
    assert(r.hexB === None)
    assert(r.sideB === None)

    assert(h.rivers(s) === r)
    assert(h.rivers.size === 1)

    r.removeFromHexes()
    assert(h.rivers.isEmpty)
  }

  test("basics - 2 hexes") {
    val h = Hex(1, HexType.CLAY)
    val h2 = Hex(2, HexType.CLAY)
    val s = Side.north
    val s2 = Side.south
    h.neighbours.update(s,h2)
    h2.neighbours.update(s2,h)
    val r = RiverSegment(h, s)

    assert(!r.hasFlow)

    assert(r.getNeighbours(inflowDirection = true).isEmpty)
    assert(r.getNeighbours(inflowDirection = false).isEmpty)

    assert(!r.flowsTowards(h))
    assert(!r.flowsTowards(h2))

    assert(r.getHexNeighbours.size === 2)
    assert(r.getHexNeighbours.contains(h))
    assert(r.getHexNeighbours.contains(h2))

    assert(r.hexA === h)
    assert(r.sideA === s)
    assert(r.hexB.value === h2)
    assert(r.sideB.value === s2)

    assert(h.rivers(s) === r)
    assert(h.rivers.size === 1)
    assert(h2.rivers(s2) === r)
    assert(h2.rivers.size === 1)

    r.removeFromHexes()
    assert(h.rivers.isEmpty)
    assert(h2.rivers.isEmpty)
  }

  test("flow east - 2 hexes") {
    val h = Hex(1, HexType.CLAY)
    val h2 = Hex(2, HexType.CLAY)
    val s = Side.north
    val s2 = Side.south
    val p = Point(1)
    val p2 = Point(2)
    h.vertices.put(Vertex.NW, p)
    h.vertices.put(Vertex.NE, p2)
    h2.vertices.put(Vertex.SW, p)
    h2.vertices.put(Vertex.SE, p2)
    h.neighbours.update(s,h2)
    h2.neighbours.update(s2,h)
    val r = RiverSegment(h, s)

    r.setFlow(fromPoint = p, toPoint = p2)

    assert(r.hasFlow)

    assert(r.getNeighbours(inflowDirection = true).isEmpty)
    assert(r.getNeighbours(inflowDirection = false).isEmpty)

    assert(r.flowsTowards(h))
    assert(r.flowsTowards(h2))

    assert(r.getHexNeighbours.size === 2)
    assert(r.getHexNeighbours.contains(h))
    assert(r.getHexNeighbours.contains(h2))

    assert(r.hexA === h)
    assert(r.sideA === s)
    assert(r.hexB.value === h2)
    assert(r.sideB.value === s2)

    assert(h.rivers(s) === r)
    assert(h.rivers.size === 1)
    assert(h2.rivers(s2) === r)
    assert(h2.rivers.size === 1)

    r.removeFromHexes()
    assert(h.rivers.isEmpty)
    assert(h2.rivers.isEmpty)
  }

  test("flow east - 4 hexes") {
    val h = Hex(1, HexType.CLAY)
    val h2 = Hex(2, HexType.CLAY)
    val h3 = Hex(3, HexType.CLAY)
    val h4 = Hex(4, HexType.CLAY)

    val hexMap = collection.mutable.AnyRefMap.empty[HexPosition, Hex]
    hexMap.update(HexPosition(1,1), h)
    hexMap.update(HexPosition(1,0), h2)
    hexMap.update(HexPosition(0,0), h3)
    hexMap.update(HexPosition(2,0), h4)

    BoardUtils.connectHexes(hexMap)

    val s = Side.north
    val s2 = Side.south
    val p = Point(1)
    val p2 = Point(2)
    h.vertices.put(Vertex.NW, p)
    h.vertices.put(Vertex.NE, p2)
    h2.vertices.put(Vertex.SW, p)
    h2.vertices.put(Vertex.SE, p2)
    h3.vertices.put(Vertex.E, p)
    h4.vertices.put(Vertex.W, p2)

    val r = RiverSegment(h, s)

    r.setFlow(fromPoint = p, toPoint = p2)

    assert(r.hasFlow)

    // rivers that it flows into/from
    assert(r.getNeighbours(inflowDirection = true).isEmpty)
    assert(r.getNeighbours(inflowDirection = false).isEmpty)

    assert(r.flowsTowards(h))
    assert(r.flowsTowards(h2))
    assert(!r.flowsTowards(h3))
    assert(r.flowsTowards(h4))

    assert(r.getHexNeighbours.size === 4)
    assert(r.getHexNeighbours.contains(h))
    assert(r.getHexNeighbours.contains(h2))
    assert(r.getHexNeighbours.contains(h3))
    assert(r.getHexNeighbours.contains(h4))

    assert(r.hexA === h)
    assert(r.sideA === s)
    assert(r.hexB.value === h2)
    assert(r.sideB.value === s2)

    assert(h.rivers(s) === r)
    assert(h.rivers.size === 1)
    assert(h2.rivers(s2) === r)
    assert(h2.rivers.size === 1)
    assert(h3.rivers.isEmpty)
    assert(h4.rivers.isEmpty)

    r.removeFromHexes()
    assert(h.rivers.isEmpty)
    assert(h2.rivers.isEmpty)
  }

  test("flow - 4 hexes, 1 other river that flows south east from it") {
    val h = Hex(1, HexType.CLAY)
    val h2 = Hex(2, HexType.CLAY)
    val h3 = Hex(3, HexType.CLAY)
    val h4 = Hex(4, HexType.CLAY)

    val hexMap = collection.mutable.AnyRefMap.empty[HexPosition, Hex]
    hexMap.update(HexPosition(1,1), h)
    hexMap.update(HexPosition(1,0), h2)
    hexMap.update(HexPosition(0,0), h3)
    hexMap.update(HexPosition(2,0), h4)

    BoardUtils.connectHexes(hexMap)

    val s = Side.north
    val s2 = Side.south
    val s3 = Side.northEast
    val s4 = Side.southWest
    val p = Point(1)
    val p2 = Point(2)
    val p3 = Point(3)
    h.vertices.put(Vertex.NW, p)
    h.vertices.put(Vertex.NE, p2)
    h2.vertices.put(Vertex.SW, p)
    h2.vertices.put(Vertex.SE, p2)
    h3.vertices.put(Vertex.E, p)
    h4.vertices.put(Vertex.W, p2)

    h.vertices.put(Vertex.E, p3)
    h4.vertices.put(Vertex.SW, p3)

    val r = RiverSegment(h, s)
    val r2 = RiverSegment(h, s3)

    r.setFlow(fromPoint = p, toPoint = p2)
    r2.setFlow(fromPoint = p2, toPoint = p3)

    // rivers that it flows into/from
    assert(r.getNeighbours(inflowDirection = true).size === 1)
    assert(r.getNeighbours(inflowDirection = true).contains(r2))
    assert(r.getNeighbours(inflowDirection = false).isEmpty)

    assert(r2.getNeighbours(inflowDirection = true).isEmpty)
    assert(r2.getNeighbours(inflowDirection = false).size === 1)
    assert(r2.getNeighbours(inflowDirection = false).contains(r))

    assert(r.flowsTowards(h))
    assert(r.flowsTowards(h2))
    assert(!r.flowsTowards(h3))
    assert(r.flowsTowards(h4))

    assert(r2.flowsTowards(h))
    assert(!r2.flowsTowards(h2))
    assert(!r2.flowsTowards(h3))
    assert(r2.flowsTowards(h4))

    assert(r.getHexNeighbours.size === 4)
    assert(r.getHexNeighbours.contains(h))
    assert(r.getHexNeighbours.contains(h2))
    assert(r.getHexNeighbours.contains(h3))
    assert(r.getHexNeighbours.contains(h4))

    assert(r2.getHexNeighbours.size === 3)
    assert(r2.getHexNeighbours.contains(h))
    assert(r2.getHexNeighbours.contains(h2))
    assert(r2.getHexNeighbours.contains(h4))

    assert(r2.hexA === h)
    assert(r2.sideA === s3)
    assert(r2.hexB.value === h4)
    assert(r2.sideB.value === s4)

    assert(h.rivers.get(s).value === r)
    assert(h.rivers.get(s3).value === r2)
    assert(h.rivers.size === 2)
    assert(h2.rivers.get(s2).value === r)
    assert(h2.rivers.size === 1)

    assert(h3.rivers.isEmpty)

    assert(h4.rivers.get(s4).value === r2)
    assert(h4.rivers.size === 1)

    r.removeFromHexes()
    assert(h.rivers.size === 1)
    assert(h2.rivers.isEmpty)
    assert(h4.rivers.size === 1)

    r2.removeFromHexes()
    assert(h.rivers.isEmpty)
    assert(h4.rivers.isEmpty)
  }

  test("flow - 4 hexes, 2 other rivers that flow south east from it, and from north east to it") {
    val h = Hex(1, HexType.CLAY)
    val h2 = Hex(2, HexType.CLAY)
    val h3 = Hex(3, HexType.CLAY)
    val h4 = Hex(4, HexType.CLAY)

    val hexMap = collection.mutable.AnyRefMap.empty[HexPosition, Hex]
    hexMap.update(HexPosition(1,1), h)
    hexMap.update(HexPosition(1,0), h2)
    hexMap.update(HexPosition(0,0), h3)
    hexMap.update(HexPosition(2,0), h4)

    BoardUtils.connectHexes(hexMap)

    val s = Side.north
    val s2 = Side.south
    val s3 = Side.northEast
    val s4 = Side.southWest
    val s5 = Side.northWest
    val s6 = Side.southEast
    val p = Point(1)
    val p2 = Point(2)
    val p3 = Point(3)
    val p4 = Point(4)
    h.vertices.put(Vertex.NW, p)
    h.vertices.put(Vertex.NE, p2)
    h2.vertices.put(Vertex.SW, p)
    h2.vertices.put(Vertex.SE, p2)
    h2.vertices.put(Vertex.E, p4)
    h3.vertices.put(Vertex.E, p)
    h4.vertices.put(Vertex.W, p2)

    h.vertices.put(Vertex.E, p3)
    h4.vertices.put(Vertex.SW, p3)

    h4.vertices.put(Vertex.NW, p4)

    val r = RiverSegment(h, s)
    val r2 = RiverSegment(h, s3)
    val r3 = RiverSegment(h4, s5)

    r.setFlow(fromPoint = p, toPoint = p2)
    r2.setFlow(fromPoint = p2, toPoint = p3)
    r3.setFlow(fromPoint = p4, toPoint = p2)

    // rivers that it flows into/from
    assert(r.getNeighbours(inflowDirection = true).size === 2)
    assert(r.getNeighbours(inflowDirection = true).contains(r2))
    assert(r.getNeighbours(inflowDirection = true).contains(r3))
    assert(r.getNeighbours(inflowDirection = false).isEmpty)

    assert(r2.getNeighbours(inflowDirection = true).isEmpty)
    assert(r2.getNeighbours(inflowDirection = false).size === 2)
    assert(r2.getNeighbours(inflowDirection = false).contains(r))
    assert(r2.getNeighbours(inflowDirection = false).contains(r3))

    assert(r3.getNeighbours(inflowDirection = true).size === 2)
    assert(r3.getNeighbours(inflowDirection = true).contains(r))
    assert(r3.getNeighbours(inflowDirection = true).contains(r2))
    assert(r3.getNeighbours(inflowDirection = false).isEmpty)

    assert(r3.flowsTowards(h))
    assert(r3.flowsTowards(h2))
    assert(!r3.flowsTowards(h3))
    assert(r3.flowsTowards(h4))

    assert(r3.getHexNeighbours.size === 3)
    assert(r3.getHexNeighbours.contains(h))
    assert(r3.getHexNeighbours.contains(h2))
    assert(r3.getHexNeighbours.contains(h4))

    assert(r3.hexA === h4)
    assert(r3.sideA === s5)
    assert(r3.hexB.value === h2)
    assert(r3.sideB.value === s6)

    assert(h.rivers.get(s).value === r)
    assert(h.rivers.get(s3).value === r2)
    assert(h.rivers.size === 2)
    assert(h2.rivers.get(s2).value === r)
    assert(h2.rivers.get(s6).value === r3)
    assert(h2.rivers.size === 2)

    assert(h3.rivers.isEmpty)

    assert(h4.rivers.get(s4).value === r2)
    assert(h4.rivers.get(s5).value === r3)
    assert(h4.rivers.size === 2)

    //flows towards rivers:
    assert(r.flowsTowards(r2))
    assert(r.flowsTowards(r3))
    assert(!r2.flowsTowards(r))
    assert(!r2.flowsTowards(r3))
    assert(r3.flowsTowards(r))
    assert(r3.flowsTowards(r2))

    r.removeFromHexes()
    assert(h.rivers.size === 1)
    assert(h2.rivers.size === 1)
    assert(h4.rivers.size === 2)

    r2.removeFromHexes()
    assert(h.rivers.isEmpty)
    assert(h2.rivers.size === 1)
    assert(h4.rivers.size === 1)

    r3.removeFromHexes()
    assert(h2.rivers.isEmpty)
    assert(h4.rivers.isEmpty)
  }

  test("set flow using from point") {

    val h = Hex(1, HexType.CLAY)
    val h2 = Hex(2, HexType.CLAY)
    val s = Side.north
    val s2 = Side.south
    val p = Point(1)
    val p2 = Point(2)
    h.vertices.put(Vertex.NW, p)
    h.vertices.put(Vertex.NE, p2)
    h2.vertices.put(Vertex.SW, p)
    h2.vertices.put(Vertex.SE, p2)
    h.neighbours.update(s,h2)
    h2.neighbours.update(s2,h)
    val r = RiverSegment(h, s)

    r.setFlowUsingFrom(p)

    assert(r.hasFlow)

    assert(r.flow.value.from === p)
    assert(r.flow.value.to === p2)
  }

  test("create works") {

    val h = Hex(1, HexType.CLAY)
    val h2 = Hex(2, HexType.CLAY)
    val h3 = Hex(3, HexType.CLAY)
    val h4 = Hex(4, HexType.CLAY)

    val hexMap = collection.mutable.AnyRefMap.empty[HexPosition, Hex]
    hexMap.update(HexPosition(1,1), h)
    hexMap.update(HexPosition(1,0), h2)
    hexMap.update(HexPosition(0,0), h3)
    hexMap.update(HexPosition(2,0), h4)

    BoardUtils.connectHexes(hexMap)

    val s = Side.north
    val s2 = Side.south
    val s3 = Side.northEast
    val s4 = Side.southWest
    val p = Point(1)
    val p2 = Point(2)
    val p3 = Point(3)
    val p4 = Point(4)
    h.vertices.put(Vertex.NW, p)
    h.vertices.put(Vertex.NE, p2)
    h.vertices.put(Vertex.E, p3)
    h2.vertices.put(Vertex.SW, p)
    h2.vertices.put(Vertex.SE, p2)
    h2.vertices.put(Vertex.E, p4)
    h3.vertices.put(Vertex.E, p)
    h4.vertices.put(Vertex.W, p2)
    h4.vertices.put(Vertex.SW, p3)
    h4.vertices.put(Vertex.NW, p4)


    var rivers = List[RiverSegment]()

    val r = RiverSegment.create(rivers, h, s)
    assert(r.isDefined)

    rivers = r.value :: rivers

    val r2 = RiverSegment.create(rivers, h, s3)
    assert(r2.isDefined)

    rivers = r2.value :: rivers

    assert(RiverSegment.create(rivers, h2, s2).isEmpty)
    assert(RiverSegment.create(rivers, h, s3).isEmpty)
    assert(RiverSegment.create(rivers, h4, s4).isEmpty)
  }

  test("equals") {

    val h = Hex(1, HexType.CLAY)
    val h2 = Hex(2, HexType.CLAY)
    val h3 = Hex(3, HexType.CLAY)
    val h4 = Hex(4, HexType.CLAY)

    val hexMap = collection.mutable.AnyRefMap.empty[HexPosition, Hex]
    hexMap.update(HexPosition(1,1), h)
    hexMap.update(HexPosition(1,0), h2)
    hexMap.update(HexPosition(0,0), h3)
    hexMap.update(HexPosition(2,0), h4)

    BoardUtils.connectHexes(hexMap)

    val rivers = List[RiverSegment]()
    val r1 = RiverSegment.create(rivers, h, Side.north)
    val r2 = RiverSegment.create(rivers, h2, Side.south)

    assert(r1 == r2)
  }
}