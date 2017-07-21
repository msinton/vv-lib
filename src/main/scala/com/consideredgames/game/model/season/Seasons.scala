package com.consideredgames.game.model.season

import com.consideredgames.serializers.{Named, NamedSetSerializer}

/**
 * Created by matt on 16/07/15.
 */
object Seasons {

  sealed abstract class SeasonType(val name: String) extends Named {}

  case object Spring extends SeasonType("spring")
  case object Summer extends SeasonType("summer")
  case object Autumn extends SeasonType("autumn")
  case object Winter extends SeasonType("winter")

  val seasonsSet = Set(Spring, Summer, Autumn, Winter)

  val serializer = new NamedSetSerializer[SeasonType](seasonsSet, Some("season"))
}
