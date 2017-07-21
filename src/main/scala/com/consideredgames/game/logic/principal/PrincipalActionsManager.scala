package com.consideredgames.game.logic.principal

import com.consideredgames.game.model.hex.Point
import com.consideredgames.game.model.info.person.skill.Skills
import com.consideredgames.game.model.info.person.skill.Skills.SkillType
import com.consideredgames.game.model.person.Person
import com.consideredgames.game.model.person.tools.{RichToolInfo, ToolUtils}
import com.consideredgames.game.model.resources.ItemContainer
import com.consideredgames.game.model.round.principal._
import com.consideredgames.game.model.player.FullPlayer

/**
 * Created by matt on 16/03/15.
 */
case class PrincipalActionsManager(private var continuedActions: List[ActionFulfillment] = Nil, var actionPoints_ : Int = 5) {

  private var actions_ : List[ActionFulfillment] = List()

  private var peopleDoingThings: List[Person] = continuedActions.foldLeft(List[Person]()) {
    case (acc, ActionFulfillment(_, PersonActionParameter(p))) => p :: acc
    case (acc, ActionFulfillment(_, TwoPersonActionParameter(p1, p2))) => p1 :: p2 :: acc
  }

  private var divertionPoints: List[Point] = List()

  def actionPoints = actionPoints_

  def actions = actions_

  def process(player: FullPlayer) = {

    for (action <- continuedActions :: actions_) {

      action match {
        case ActionFulfillment(actio: PersonAction with SkillLevelProvider, PersonActionParameter(p)) =>

          if (actio.isPossible(p)) {
            degradeUsedTools(p, actio.skill, actio.toolUtils, player.itemContainer)

            // TODO process the action!
          }

        //TODO case divert action
        case Nil => //do nothing
      }

    }
  }

  def addAction(action: Action, point: Point): Unit = if (addAction(action, PointActionParameter(point))) divertionPoints = point :: divertionPoints

  def addAction(action: Action, person: Person): Unit =
    if (addAction(action, PersonActionParameter(person))) peopleDoingThings = person :: peopleDoingThings

  def addAction(action: Action, p1: Person, p2: Person): Unit =
    if (addAction(action, TwoPersonActionParameter(p1, p2))) peopleDoingThings = p1 :: p2 :: peopleDoingThings

  def addAction(action: Action): Unit = addAction(action, EmptyActionParameter())

  private def addAction(action: Action, actionParameter: ActionParameter): Boolean = {

    if (actionPoints_ > 0) {
      actionParameter match {
        case PersonActionParameter(p) if peopleDoingThings.contains(p) => removeAction(p)
        case TwoPersonActionParameter(p1, p2) =>
          if (peopleDoingThings.contains(p1)) removeAction(p1)
          if (peopleDoingThings.contains(p2)) removeAction(p2)
        case PointActionParameter(point) if divertionPoints.contains(point) => return false
        case _ => // do nothing todo - is this correct?
      }
      actions_ = ActionFulfillment(action, actionParameter) :: actions_
      actionPoints_ -= 1
      true
    } else {
      false
    }
  }

  def removeAction(person: Person): Unit = {
    actions_ = actions_.filterNot {
      case ActionFulfillment(a, PersonActionParameter(p)) if p == person =>
        peopleDoingThings = peopleDoingThings.filterNot(_ == p)
        actionPoints_ += 1
        true
      case ActionFulfillment(a, TwoPersonActionParameter(p1, p2)) if p1 == person || p2 == person =>
        peopleDoingThings = peopleDoingThings.filterNot(p => p == p1 || p == p2)
        actionPoints_ += 1
        true
    }

    continuedActions = continuedActions.filterNot {
      case ActionFulfillment(a, PersonActionParameter(p)) if p == person =>
        peopleDoingThings = peopleDoingThings.filterNot(_ == p)
        true
      case ActionFulfillment(a, TwoPersonActionParameter(p1, p2)) if p1 == person || p2 == person =>
        peopleDoingThings = peopleDoingThings.filterNot(p => p == p1 || p == p2)
        true
    }
  }

  def removeAction(point: Point): Unit = {
    actions_ = actions_.filterNot {
      case ActionFulfillment(_, PointActionParameter(p)) if p == point =>
        divertionPoints = divertionPoints.filterNot(_ == point)
        actionPoints_ += 1
        true
    }
  }

  // must not degrade more tools than were needed to max out the skill
  //TODO need to skip tools that would give no net benefit!
  private def degradeUsedTools(person: Person, skillType: SkillType, toolUtils: ToolUtils, itemContainer: ItemContainer): Unit = {

    def getToolsToSkip: Set[RichToolInfo] = {
      //TODO toolBonus + skillLevel in another class totalLevel(person, skillType) -depends on ToolUtils and SkillUtils
      val bonusExcess = toolUtils.totalBonuses(person.tools, skillType) + Skills.skillLevel(person, skillType) - skillType.maxLevel
      if (bonusExcess > 0) {
        //work out what tools if any to remove
        val candidates = person.tools.filter { t =>
          val bonus = t.bonuses.getOrElse(skillType, 0)
          bonus <= bonusExcess
        }

        val candidatesList = candidates.toList.sortBy(_.bonuses.getOrElse(skillType, 0))

        if (candidatesList.nonEmpty) {
          //try simple case
          if (candidatesList.last.bonuses.get(skillType).contains(bonusExcess)) return Set(candidates.last)

          var bestMatch = (0, List[RichToolInfo]())
          for (combo <- candidatesList.combinations(bonusExcess)) {
            var total = 0
            val comboItr = combo.iterator
            var tools: List[RichToolInfo] = List()
            while (total < bonusExcess && comboItr.hasNext) {
              val t = comboItr.next()
              if (total > bestMatch._1) bestMatch = (total, tools)
              total += t.bonuses(skillType)
              tools = t :: tools
            }
            if (total == bonusExcess) {
              bestMatch = (total, tools)
            }
            if (bestMatch._1 == bonusExcess) return bestMatch._2.toSet
          }
          return bestMatch._2.toSet
        }
      }
      Set()
    }

    val toolsToSkip: Set[RichToolInfo] = getToolsToSkip

    for (t <- person.tools.diff(toolsToSkip)) {
      itemContainer.consume(t, person)
    }
  }

}
