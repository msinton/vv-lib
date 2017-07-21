package com.consideredgames.game.model.round.principal

import com.consideredgames.game.model.animals.AnimalInfo
import com.consideredgames.game.model.info.person.skill.Skills.SkillType
import com.consideredgames.game.model.person.Person
import com.consideredgames.game.model.person.tools.ToolProduct
import com.consideredgames.game.model.resources.ResourceProduct

/**
 * Created by matt on 16/04/15.
 */
sealed trait ActionResult

case object EmptyResult extends ActionResult

trait PersonResult {
  def xpGain: Int
  def skill: SkillType
}

case class ResourceResult(result: ResourceProduct, xpGain: Int, skill: SkillType) extends ActionResult with PersonResult

case class CraftingResult(result: List[ToolProduct], xpGain: Int, skill: SkillType) extends ActionResult with PersonResult

case class PersonProductionResult(percent: Int, mate: Person, xpGain: Int, skill: SkillType) extends ActionResult with PersonResult

case class HuntingResult(percents: List[Int], animal: AnimalInfo, xpGain: Int, skill: SkillType) extends ActionResult with PersonResult

case class AttackDamageResult(amount: Int, attacked: Person, xpGain: Int, skill: SkillType) extends ActionResult with PersonResult

case class ArableResult(weatherResourceProduct: WeatherResourceProduct, xpGain: Int, skill: SkillType) extends ActionResult with PersonResult

case class DefendResult(xpGain: Int, skill: SkillType) extends ActionResult with PersonResult

case class HarvestResult(number: Int, animal: AnimalInfo) extends ActionResult

object ActionResults {

  val classes: List[Class[_]] = List(classOf[ResourceResult], classOf[CraftingResult], classOf[PersonProductionResult],
    classOf[HuntingResult], classOf[AttackDamageResult], classOf[ArableResult], classOf[DefendResult], classOf[HarvestResult])
}