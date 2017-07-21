package com.consideredgames.game.model.deployment

import com.consideredgames.game.model.hex.{Hex, Side}

/**
 * movement/deployment location
 */
trait Location

case class HexLocation(hex: Hex) extends Location

case class BorderLocation(hex: Hex, side: Side) extends Location

object Location {
  val classes = List(classOf[HexLocation], classOf[BorderLocation])
}