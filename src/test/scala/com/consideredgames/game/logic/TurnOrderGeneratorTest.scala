package com.consideredgames.game.logic

import com.consideredgames.game.model.player._
import org.scalatest.FunSuite

import scala.util.Random

/**
 * Created by matt on 08/04/15.
 */
class TurnOrderGeneratorTest extends FunSuite {

  object PlayerStub1 extends OtherPlayer("1", null)

  object PlayerStub2 extends OtherPlayer("2", null)

  object PlayerStub3 extends OtherPlayer("3", null)

  test("that generates a random ordering") {

    val playerData = List(PlayerStub1, PlayerStub2, PlayerStub3)

    var order = TurnOrderGenerator.generate(playerData, new Random(1))

    assert(order == List("2", "3", "1"))

    order = TurnOrderGenerator.generate(playerData, new Random(0))

    assert(order == List("3", "2", "1"))
  }

  test("that generates a random ordering for 2") {

    val playerData = List(PlayerStub1, PlayerStub2)

    var order = TurnOrderGenerator.generate(playerData, new Random(1))

    assert(order == List("1", "2"))

    order = TurnOrderGenerator.generate(playerData, new Random(4096))

    assert(order == List("2", "1"))
  }

}
