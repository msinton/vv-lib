package com.consideredgames.game.logic.setup

/**
 * Created by matt on 15/10/15.
 */
object StartingTools {

  val startingToolsDefault = List(
    ToolOrder("stone axe", 1),
    ToolOrder("stone hammer", 1),
    ToolOrder("wooden mallet", 1),
    ToolOrder("fishing rod", 1)
  )
}

case class ToolOrder(name: String, n: Int)