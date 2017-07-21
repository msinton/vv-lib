package com.consideredgames.game.model.person.tools

import com.consideredgames.game.model.info.person.skill.Skills.SkillType
import com.consideredgames.game.model.person.Person
import com.consideredgames.game.model.resources.ItemContainer

import scala.collection.GenTraversable

/**
 * Created by matt on 16/03/15.
 */
case class ToolUtils(allToolInfo: Tools) {

  /**
   * @return (false,x) if the person does not have a needed core tool - and then helpfully x contains the core tools
   */
  def coreRequirementSatisfied(person: Person, skillType: SkillType): (Boolean, Option[collection.Set[RichToolInfo]]) = {

    // does the skill require a core item
    allToolInfo.core.get(skillType) match {
      // if it does then proceed, does the person have one of these?
      case x@Some(xs) if xs.nonEmpty =>
        person.tools.intersect(xs) match {
          case inBoth if inBoth.nonEmpty => (true, Option(inBoth))
          case _ => (false, x)
        }

      case _ => (true, None)
    }
  }

  /**
   * @return (true, x) if the person does not have a needed core tool then helpfully x contains any of those core tools which are available
   */
  def canMeetCoreRequirement(person: Person, skillType: SkillType, availableTools: collection.Set[RichToolInfo]): (Boolean, collection.Set[RichToolInfo]) = {

    coreRequirementSatisfied(person, skillType) match {
      case (true,_) => (true, Set())
      case (false, coreTools) =>
        val tools = allToolInfo.core(skillType).toSet.intersect(availableTools)
        if (tools.isEmpty) {
          (false, tools)
        } else {
          (true, tools)
        }
    }
  }

  /**
   * Gets the interchanges for a skill - there may be multiple sets that within themselves are interchangeable
   * @param skillType
   * @return
   */
  def getInterchanges(skillType: SkillType): List[Set[RichToolInfo]] = {
    allToolInfo.interchangeables filter
      (interchanges => interchanges.skills == None || interchanges.skills.get.contains(skillType)) map (_.set)
  }

  def bestBonus(tools: Traversable[RichToolInfo], skillType: SkillType) = {
    tools.max(RichToolInfo.orderingBySkillBonus(skillType))
  }

  /**
   * @return the total skill bonus that will be given to the person for the skillType
   */
  def getSkillBonus(person: Person, skillType: SkillType): Int = {

    if (!coreRequirementSatisfied(person, skillType)._1)
      0
    else {
      // negate the lesser interchangeables
      val toolsWithBonusForSkill = allToolInfo.bonuses(skillType).intersect(person.tools)
      val bonuses = filterOutInterchangeablesChooseBest(toolsWithBonusForSkill, skillType)
      totalBonuses(bonuses, skillType)
    }
  }

  def totalBonuses(tools: GenTraversable[RichToolInfo], skillType: SkillType): Int = {
    tools.foldLeft(0)((a: Int, c: RichToolInfo) => a + c.bonuses(skillType))
  }

  /**
   * Tries to assign the tool with the best bonus, from the unassigned supply, unless person already has the best available.
   * @return true if assigned/ already had best tool
   */
  private def assignBestOutOf(itemContainer: ItemContainer, tools: Set[RichToolInfo], skillType: SkillType, person: Person): Boolean = {
    val alreadyOwned = tools.intersect(person.tools)

    val sortedTools = tools.toList.sorted(RichToolInfo.orderingBySkillBonus(skillType).reverse)
    for (t <- sortedTools) {
      if (alreadyOwned.nonEmpty) {
        if (alreadyOwned.exists(_.bonuses.get(skillType).exists(owned => owned >= t.bonuses.getOrElse(skillType, -1)))) {
          // they already have the best tool
          return true
        }
      }
      if (person.tools.contains(t) || itemContainer.assign(t, person))
        return true
    }
    false
  }

  def autoAssignBestAvailableTools(itemContainer: ItemContainer, person: Person, skillType: SkillType): Boolean = {

    // if they cant get a core tool then make no changes - return false
    // o.w. assign best tools, taking into account already assigned to it

    val gotCore: Boolean = coreRequirementSatisfied(person, skillType) match {
      case (true, _) => true

      case (false, Some(y)) =>
        // can get a core tool?
        val available = itemContainer.availableTools(y)
        // find best one
        if (available.nonEmpty)
          itemContainer.assign(bestBonus(available, skillType), person) //kind of redundant, but better to fail early - testing required!
        else
          false

      case _ => false
    }

    if (gotCore) {
      val interchanges: List[Set[RichToolInfo]] = getInterchanges(skillType)
      // look for each of the tools for the skill
      // if found and that tool is interchangeable with other tools (for the skill) then choose the best one

      // assign best of each interchangeable set
      for (inter <- interchanges) {
        // find the max one that we have. Sort into a list and start with the best until got one or none left
        assignBestOutOf(itemContainer, inter, skillType, person)
      }

      val allInters = interchanges.flatten
      // assign the rest
      for (toolInfoOpt <- allToolInfo.bonuses.get(skillType);
           toolInfo <- toolInfoOpt) {
        // skip the tools which are in the interchangeables
        if (!allInters.contains(toolInfo)) {
          itemContainer.assign(toolInfo, person)
        }
      }

      //TODO handle giving more tools than is beneficial i.e. the skill bonus is maxed out
      true

    } else {
      false
    }
  }

  def filterOutInterchangeablesChooseBest(tools: Set[RichToolInfo], skillType: SkillType) = {

    val interchanges = getInterchanges(skillType)
    var reducedTools = tools

    for (inter <- interchanges) {

      val intersection = tools.intersect(inter)
      if (intersection.nonEmpty) {
        val bestTool = intersection.toList.max(RichToolInfo.orderingBySkillBonus(skillType))
        reducedTools = reducedTools.diff(intersection - bestTool)
      }
    }
    reducedTools
  }
}
