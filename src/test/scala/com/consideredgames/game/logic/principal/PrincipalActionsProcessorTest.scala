package com.consideredgames.game.logic.principal

import com.consideredgames.game.logic.season.WeatherManager
import com.consideredgames.game.model.animals.AnimalInfo
import com.consideredgames.game.model.info.person.skill.SkillStat
import com.consideredgames.game.model.info.person.skill.Skills._
import com.consideredgames.game.model.person.Person
import com.consideredgames.game.model.person.tools._
import com.consideredgames.game.model.player.FullPlayer
import com.consideredgames.game.model.player.PlayerColours._
import com.consideredgames.game.model.resources.ItemContainer
import com.consideredgames.game.model.round.principal._
import com.consideredgames.game.model.season.WeatherWeightings
import com.consideredgames.message.PersonToActionResult
import org.scalatest.FunSuite

import scala.collection.mutable
import scala.util.Random


class PrincipalActionsProcessorTest extends FunSuite {

  val tools = Tools("/5Tools.txt")

  val toolUtils = ToolUtils(tools)

  val actions = new Actions(toolUtils, AnimalInfo.importFromFile().get)

  val attackXp = actions.Attack.xpGain
  val defendXp = actions.Defend.xpGain

  val random = new Random(1)

  val principalActionsProcessor = new PrincipalActionsProcessor(actions, random, new WeatherManager(WeatherWeightings.defaultEasy, random), null)

  test("attacker and defender, attacker dies") {

    val itemContainer = new ItemContainer(toolUtils)

    object player1 extends FullPlayer("name1", itemContainer, Black)

    object defender extends Person(1) {
      hp = 1

      override def playerColour = Black
    }
    object attacker extends Person(2) {
      hp = 1

      override def playerColour = Blue
    }

    val player1Actions = List(ActionFulfillment(actions.Defend, PersonActionParameter(defender)), ActionFulfillment(actions.Attack, TwoPersonActionParameter(attacker, defender)))

    val result = principalActionsProcessor.preProcessForServer(List((player1, player1Actions)))

    val map = mutable.AnyRefMap.empty[PlayerColour, Set[Person]]
    map.update(Blue, Set(attacker))

    assert(result._1 === AttackResults(List(Damage(1, attacker, defender), Damage(1, defender, attacker)), map))
    assert(result._2 === List((defender, 1, Defender)))
    assert(result._3 === List((player1, List((defender, DefendResult(1, Defender))))))

    val resultsByPlayer = principalActionsProcessor.actionResultsByPlayerForServer(player1, result._1, result._3, result._4, result._5)

    assert(resultsByPlayer.allActionResults === List(PersonToActionResult(defender, DefendResult(1, Defender))))
    assert(resultsByPlayer.attackResults === AttackResults(List(Damage(1, attacker, defender), Damage(1, defender, attacker)), map))
  }

  test("attacker and defender, defender dies") {

    val itemContainer = new ItemContainer(toolUtils)

    object player1 extends FullPlayer("name1", itemContainer, null)

    object defender extends Person(1) {
      hp = 1

      override def playerColour = Black
    }

    object attacker extends Person(2) {
      hp = 1
      skills.update(Attacker, SkillStat(1))

      override def playerColour = Blue
    }

    val player1Actions = List(ActionFulfillment(actions.Defend, PersonActionParameter(defender)), ActionFulfillment(actions.Attack, TwoPersonActionParameter(attacker, defender)))

    val result = principalActionsProcessor.preProcessForServer(List((player1, player1Actions)))

    val map = mutable.AnyRefMap.empty[PlayerColour, Set[Person]]
    map.update(Black, Set(defender))

    assert(result._1 === AttackResults(List(Damage(2, attacker, defender)), map))
    assert(result._2 === List())
    assert(result._3 === List((player1, List((attacker, AttackDamageResult(2, defender, 2, Attacker))))))
  }

  test("two attacks on same defender, defender dies") {

    val itemContainer = new ItemContainer(toolUtils)

    object player1 extends FullPlayer("name1", itemContainer, null)

    object defender extends Person(1) {
      hp = 2

      override def playerColour = Black
    }

    object attacker1 extends Person(2) {
      hp = 2

      override def playerColour = Blue
    }

    object attacker2 extends Person(3) {
      hp = 2

      override def playerColour = Blue
    }

    val map = mutable.AnyRefMap.empty[PlayerColour, Set[Person]]
    map.update(Black, Set(defender))

    val player1Actions = List(ActionFulfillment(actions.Attack, TwoPersonActionParameter(attacker1, defender)), ActionFulfillment(actions.Attack, TwoPersonActionParameter(attacker2, defender)))

    val result = principalActionsProcessor.preProcessForServer(List((player1, player1Actions)))

    assert(result._1 === AttackResults(List(Damage(1, attacker1, defender), Damage(1, attacker2, defender)), map))
    assert(result._2 === List())
    assert(result._3 === List((player1, List((attacker1, AttackDamageResult(1, defender, 2, Attacker)), (attacker2, AttackDamageResult(1, defender, 2, Attacker))))))
  }

  test("two attacks on same defender, defender lives and gains xp") {

    val itemContainer = new ItemContainer(toolUtils)

    object player1 extends FullPlayer("name1", itemContainer, null)

    object defender extends Person(1) {
      hp = 3

      override def playerColour = Black
    }

    object attacker1 extends Person(2) {
      hp = 2

      override def playerColour = Blue
    }

    object attacker2 extends Person(3) {
      hp = 2

      override def playerColour = Blue
    }

    val player1Actions = List(ActionFulfillment(actions.Attack, TwoPersonActionParameter(attacker1, defender)), ActionFulfillment(actions.Attack, TwoPersonActionParameter(attacker2, defender)))

    val result = principalActionsProcessor.preProcessForServer(List((player1, player1Actions)))

    val map = mutable.AnyRefMap.empty[PlayerColour, Set[Person]]

    val expect = (AttackResults(List(Damage(1, attacker1, defender), Damage(1, attacker2, defender), Damage(1, defender, attacker1), Damage(1, defender, attacker2)),
      map), List((defender, defendXp, Defender)))

    implicit val damageOrdering: Ordering[Damage] = Ordering.by(e => (e.from, e.to, e.amount))

    assert(result._1.damages.sorted == expect._1.damages.sorted)
    assert(result._1.deaths.keys.isEmpty)
    assert(result._2.sortBy { case (p, xp, skill) => p.playerColour.name } == expect._2.sortBy { case (p, xp, skill) => p.playerColour.name })
  }

}
