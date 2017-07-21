package com.consideredgames.game.model.player

import com.consideredgames.game.model.deployment.HexLocation
import com.consideredgames.game.model.hex.{Hex, HexType}
import com.consideredgames.game.model.person.{NewPersonInstruction, Person}
import com.consideredgames.game.model.player.PlayerColours._
import org.scalatest.{FunSuite, OptionValues}

/**
 * Created by matt on 09/09/15.
 */
class PlayerWithPeopleTest extends FunSuite with OptionValues {

  test("that can create a person with an Id and colour") {
    val player = new OtherPlayer("bob", Blue)

    val person = player.create(NewPersonInstruction(1, Blue, Option(HexLocation(Hex(1, HexType.PLAINS)))))

    assert(player.person(1).value === person)
  }

  test("canAddPerson") {
    val player = new OtherPlayer("bob", Blue)
    player.create(NewPersonInstruction(0, Blue))
    assert(player.canAddPerson(1, Blue))
  }

  test("canAddPerson =false, not same colour") {
    val player = new OtherPlayer("bob", Blue)

    assert(player.canAddPerson(1, Black) === false)
  }

  test("canAddPerson =false, person exists") {
    val player = new OtherPlayer("bob", Blue)
    player.create(NewPersonInstruction(1, Blue))
    assert(player.canAddPerson(1, Blue) === false)
  }

  test("kill") {
    val player = new OtherPlayer("bob", Blue)
    player.create(NewPersonInstruction(0, Blue))
    assert(player.kill(Person(0, Blue)).value.id === 0)
  }

}
