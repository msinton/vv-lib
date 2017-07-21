package com.consideredgames.game.model.hex

/**
 * Created by Matt.Sinton-Hewitt on 23/03/2015.
 */
object PointInitialiser {

  def setupPoints(hexes: Iterator[Hex], pointFactory: PointFactory): Unit = {
    for (hex <- hexes) {
      setupVertices(hex, pointFactory)
    }
  }

  /**
   * Sets the points for hexes in their "trio" surrounding a single point. Does this for the points shared by
   * the hex and its neighbours.
   */
  private def setupVertices(hex: Hex, pointFactory: PointFactory) {
    for ((side,hexA) <- hex.neighbours) {
      val hexB = hex.neighbours.get(side.anticlockwise())
      val vertex = side.anticlockwiseVertex()

      val vertexA = side.opposite().clockwiseVertex()
      var hexes: List[(Hex, Vertex)] = (hexA, vertexA) :: Nil

      hexB foreach { h =>
        val vertexB = side.anticlockwise().opposite().anticlockwiseVertex()
        hexes = (h, vertexB) :: hexes
      }

      if (hex.vertices.contains(vertex)) {
        val point = hex.vertices.get(vertex)
        point foreach { p =>
          // check that other hexes contain the same point, error if not.
          for ((h,v) <- hexes) {
            if (!h.vertices.get(v).contains(p)) {
              throw new RuntimeException(
                "A hex has been assigned a point, which is not the same point as one of its neighbouring hexes, which should share this point."
                  + " Hex " + this + " neighbours " + hexes + " point " + p)
            }
          }
        }
      } else {
        // new point for all hexes
        hexes = (hex, vertex) :: hexes
        val point = pointFactory.build()
        for ((h,v) <- hexes) {

          h.vertices.update(v, point)
          point.setOneOfThreeHexes(v, h)
        }
      }
    }
  }
}