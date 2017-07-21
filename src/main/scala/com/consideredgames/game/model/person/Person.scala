package com.consideredgames.game.model.person

import com.consideredgames.game.model.animals.AnimalManager
import com.consideredgames.game.model.hex.{Boat, Hex}
import com.consideredgames.game.model.info.person.skill.SkillStat
import com.consideredgames.game.model.info.person.skill.Skills.SkillType
import com.consideredgames.game.model.person.tools.RichToolInfo
import com.consideredgames.game.model.player.PlayerColours.PlayerColour

/**
 * The person which the player controls. The person may be carrying animals - these
 * are maintained by the AnimalManager.
 *
 * Created by matt on 11/03/15.
 */
case class Person protected(id: Int) extends Comparable[Person] {

  private var playerColour_ : PlayerColour = _

  var hex: Option[Hex] = None

  var boat: Option[Boat] = None

  val tools = collection.mutable.Set.empty[RichToolInfo]

  val skills = collection.mutable.AnyRefMap.empty[SkillType, SkillStat]

  var animalManager: Option[AnimalManager] = None

  def playerColour = playerColour_

  var hp: Int = 1

  override def compareTo(o: Person): Int = playerColour.name.compareTo(o.playerColour.name)
}

object Person {

  def apply(id: Int, playerColour: PlayerColour) = {
    val p = new Person(id)
    p.playerColour_ = playerColour
    p
  }

  def apply(newPersonInstruction: NewPersonInstruction): Person = {
    Person(newPersonInstruction.id, newPersonInstruction.playerColour)
  }

  val ordering: Ordering[Person] = Ordering.by(_.playerColour.name)
}