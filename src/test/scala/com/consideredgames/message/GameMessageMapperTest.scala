package com.consideredgames.message

import com.consideredgames.game.model.animals.{Animal, AnimalInfo}
import com.consideredgames.game.model.deployment.HexLocation
import com.consideredgames.game.model.hex.{Hex, HexType}
import com.consideredgames.game.model.info.person.skill.Skills.Lumberjack
import com.consideredgames.game.model.person.Person
import com.consideredgames.game.model.person.tools.Tools
import com.consideredgames.game.model.resources.{ResourceGroup, ResourceProduct}
import com.consideredgames.game.model.resources.Resources.Wood
import com.consideredgames.game.model.round.principal.{AttackResults, Damage, ResourceResult}
import com.consideredgames.game.model.player.PlayerColours
import com.consideredgames.game.model.player.PlayerColours._
import com.consideredgames.message.Messages.Join
import org.scalatest.{FunSuite, TryValues}

import scala.collection.mutable

/**
  * Created by matt on 20/04/17.
  */
class GameMessageMapperTest extends FunSuite with TryValues {
  val animalInfos = AnimalInfo.importFromFile().get
  val messageMapper = new GameMessageMapper(animalInfos, new Tools())

  test("to bytes and back") {
    val m = Movements(Seq(PersonMovement(Person(1, PlayerColours.Black), HexLocation(Hex(1, HexType.CLAY)))))

    val json = messageMapper.toJson(m)
    val result = messageMapper.toMessage(json)

    assert(m == result)
  }

  test("PrincipalPhasePreProcess") {

    val defender = Person(1, Black)
    val attacker = Person(2, Blue)

    val map = mutable.AnyRefMap.empty[PlayerColour, Set[Person]]
    map.update(Blue, Set(attacker))

    val m = PrincipalPhasePreProcess(
      AttackResults(List(Damage(1,attacker,defender), Damage(1,defender,attacker)), map),
      List(PersonToActionResult(Person(1, Black), ResourceResult(ResourceProduct(List(ResourceGroup(Wood, 1))), 2, Lumberjack))),
      List(),
      List())

    val json = messageMapper.toJson(m)
    val result = messageMapper.toMessage(json)

    assert(m == result)
  }

  test("PrincipalPhasePreProcess with animals killed") {

    val m = PrincipalPhasePreProcess(
      AttackResults(List(), Map()),
      List(),
      List(Animal(animalInfos(0), true)),
      List())

    val json = messageMapper.toJson(m)
    val result = messageMapper.toMessage(json)

    assert(m == result)
  }
}
