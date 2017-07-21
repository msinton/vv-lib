package com.consideredgames.game.model.player

import com.consideredgames.serializers.{Named, NamedSetSerializer}


/**
 * Created by matt on 26/06/15.
 */
object PlayerColours {

  sealed abstract class PlayerColour(val name: String) extends Named {
  }

  case object Blue extends PlayerColour("blue")
  case object Black extends PlayerColour("black")
  case object Purple extends PlayerColour("purple")
  case object Grey extends PlayerColour("grey")
  case object LightGreen extends PlayerColour("lightGreen")
  case object DarkGreen extends PlayerColour("darkGreen")

  val playerColoursSet: Set[PlayerColour] = Set(Blue, Black, Purple, Grey, LightGreen, DarkGreen)

  val playerColours = playerColoursSet.toList.sorted

  val serializer = new NamedSetSerializer[PlayerColour](playerColoursSet, Some("playerColour"))
}
