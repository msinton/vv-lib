package com.consideredgames.game.model.resources

import com.consideredgames.serializers.{Named, NamedSetSerializer}
import scala.language.implicitConversions

object Resources {

  sealed abstract class Resource(val name: String, val worth: Int) extends Named {
  }

  case object Wood extends Resource(name = "wood", worth = 0)

  case object Stone extends Resource(name = "stone", worth = 0)

  case object IronOre extends Resource(name = "iron ore", worth = 0)

  case object Clay extends Resource(name = "clay", worth = 0)

  case object Coal extends Resource(name = "coal", worth = 1)

  case object Feathers extends Resource(name = "feathers", worth = 1)

  case object Bone extends Resource(name = "bone", worth = 0)

  case object Gold extends Resource(name = "gold", worth = 4)

  case object Hide extends Resource(name = "hide", worth = 1)

  case object Sticks extends Resource(name = "sticks", worth = 0)

  case object Diamond extends Resource(name = "diamond", worth = 10)

  case object Food extends Resource(name = "food", worth = 0)

  case object Meal extends Resource(name = "meal", worth = 1)

  case object GoodMeal extends Resource(name = "good meal", worth = 2)

  val resourcesSet = Set(Wood, Stone, IronOre, Clay, Coal, Feathers, Bone, Gold, Hide, Sticks, Diamond, Food, Meal, GoodMeal)

  val resources = resourcesSet.toList.sorted

  implicit def orderingByWorthThenName[A <: Resource]: Ordering[A] = Ordering.by(e => (e.worth, e.name))

  val serializer = new NamedSetSerializer[Resource](resourcesSet, Some("res"))

  object ResourceImplicits {

    implicit def Set2SortedList[A <: Resource](set: Set[A]): List[A] = set.toList.sorted

  }

}
