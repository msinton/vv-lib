package com.consideredgames.game.logic.deployment

import com.consideredgames.game.model.animals.AnimalInfo
import com.consideredgames.game.model.board.{BoardData, BoardUtils}
import com.consideredgames.game.model.deployment.{BorderLocation, HexLocation}
import com.consideredgames.game.model.hex.{Boat, Hex, HexType, Side}
import com.consideredgames.game.model.person.{NewPersonInstruction, Person}
import com.consideredgames.game.model.person.tools.{ToolUtils, Tools}
import com.consideredgames.game.model.player._
import com.consideredgames.game.model.player.PlayerColours._
import com.consideredgames.game.model.resources.ItemContainer
import org.scalatest.{FunSuite, OptionValues}

import scala.util.Random

/**
 * Created by matt on 12/09/15.
 */
class DeploymentProcessorTest extends FunSuite with OptionValues {

  val boardUtils = new BoardUtils(new BoardData(2, new Random(), AnimalInfo.importFromFile().get))
  val deployProcessor = new DeploymentProcessor(boardUtils)
  val toolUtils = new ToolUtils(new Tools())

  test("validate OK for just person") {
    val player = OtherPlayer("Bob", Black)

    assert(deployProcessor.valid(List(NewPersonInstruction(1, Black)), player)._1)
  }

  test("validate OK for person on hex") {
    val player = OtherPlayer("Bob", Black)

    val result = deployProcessor.valid(List(NewPersonInstruction(1, Black, Option(HexLocation(Hex(1, HexType.PLAINS))))), player)
    assert(result._1)
  }

  test("validate OK for person on boat") {
    val boardUtils = new BoardUtils(new BoardData(2, new Random(), AnimalInfo.importFromFile().get))
    val deployProcessor = new DeploymentProcessor(boardUtils)
    val player = OtherPlayer("Bob", Black)
    val hex = boardUtils.boardData.getHex(1).value
    val side = Side.south
    Boat(hex, side)

    val result = deployProcessor.valid(List(NewPersonInstruction(1, Black, Option(BorderLocation(Hex(1, HexType.PLAINS), side)))), player)
    assert(result._1)
  }

  test("validate BAD for person not same colour as player") {
    val player = OtherPlayer("Bob", Black)
    val instr = NewPersonInstruction(1, Black)
    player.create(instr)

    assert(deployProcessor.valid(List(NewPersonInstruction(2, Blue)), player)._1 === false)
  }

  test("validate BAD for person id already exists") {
    val player = OtherPlayer("Bob", Black)
    val instr = NewPersonInstruction(1, Black)
    player.create(instr)

    assert(deployProcessor.valid(List(NewPersonInstruction(1, Black)), player)._1 === false)
  }

  test("validate BAD for hex not on board") {
    val player = OtherPlayer("Bob", Black)

    assert(deployProcessor.valid(List(NewPersonInstruction(1, Black, Option(HexLocation(Hex(99999, HexType.PLAINS))))), player)._1 === false)
  }

  test("validate BAD for boat does not exist") {
    val player = OtherPlayer("Bob", Black)

    assert(deployProcessor.valid(List(NewPersonInstruction(1, Black, Option(BorderLocation(Hex(1, HexType.PLAINS), Side.south)))), player)._1 === false)
  }

  test("validate BAD for boat full") {
    val boardUtils = new BoardUtils(new BoardData(2, new Random(), AnimalInfo.importFromFile().get))
    val deployProcessor = new DeploymentProcessor(boardUtils)
    val player = OtherPlayer("Bob", Black)
    val hex = boardUtils.boardData.getHex(1).value
    val side = Side.south
    val boat = Boat(hex, side)
    boat.addPerson(Person(2, Black))
    boat.addPerson(Person(3, Black))

    assert(deployProcessor.valid(List(NewPersonInstruction(1, Black, Option(BorderLocation(Hex(1, HexType.PLAINS), side)))), player)._1 === false)
  }

  test("deploy to hexes") {
    val boardUtils = new BoardUtils(new BoardData(2, new Random(), AnimalInfo.importFromFile().get))
    val deployProcessor = new DeploymentProcessor(boardUtils)
    val player = FullPlayer("Frank", new ItemContainer(toolUtils), Black)

    val newPeople = List(NewPersonInstruction(1, Black, Option(HexLocation(Hex(1, HexType.PLAINS)))),
      NewPersonInstruction(2, Black, Option(HexLocation(Hex(2, HexType.PLAINS)))))

    deployProcessor.deploy(newPeople, player)

    val hex1 = boardUtils.boardData.getHex(1).value
    val hex2 = boardUtils.boardData.getHex(2).value

    assert(hex1.person.exists(_.id === 1))
    assert(hex2.person.exists(_.id === 2))
    assert(player.person(1).value === Person(1, Black))
    assert(player.person(2).value === Person(2, Black))
    assert(hex1.person.value.hex.value === hex1)
    assert(hex2.person.value.hex.value === hex2)
  }

  test("deploy to boats") {
    val boardUtils = new BoardUtils(new BoardData(2, new Random(), AnimalInfo.importFromFile().get))
    val deployProcessor = new DeploymentProcessor(boardUtils)
    val player = FullPlayer("Frank", new ItemContainer(toolUtils), Black)

    val hex1 = boardUtils.boardData.getHex(1).value
    val hex2 = boardUtils.boardData.getHex(2).value
    val side = Side.south
    val boat1 = Boat(hex1, side)
    val boat2 = Boat(hex2, side)

    val newPeople = List(NewPersonInstruction(1, Black, Option(BorderLocation(Hex(1, HexType.PLAINS), side))),
      NewPersonInstruction(2, Black, Option(BorderLocation(Hex(2, HexType.PLAINS), side))))

    deployProcessor.deploy(newPeople, player)

    assert(boat1.people === List(Person(1, Black)))
    assert(boat2.people === List(Person(2, Black)))
    assert(player.person(1).value === Person(1, Black))
    assert(player.person(2).value === Person(2, Black))
    assert(player.person(1).value.boat.value === boat1)
    assert(player.person(2).value.boat.value === boat2)
  }

  test("deploy boats") {
    val boardData = new BoardData(2, new Random(), AnimalInfo.importFromFile().get)
    val boardUtils = new BoardUtils(boardData)
    val deployProcessor = new DeploymentProcessor(boardUtils)

    val boatM1 = Boat(Hex(1, HexType.CLAY), Side.north)
    val boatM2 = Boat(Hex(2, HexType.CLAY), Side.south)
    val boatM3 = Boat(Hex(3, HexType.CLAY), Side.northEast)

    val boats = List(boatM1, boatM2, boatM3)

    deployProcessor.deploy(boats)
    assert(boardData.boats.size === 3)
    assert(boardData.getHex(1).value.boats(Side.north) == boatM1)
    assert(boardData.getHex(2).value.boats(Side.south) == boatM2)
    assert(boardData.getHex(3).value.boats(Side.northEast) == boatM3)
    assert(boardData.getHex(1).value.boats(Side.north).hexA === boardData.getHex(1).value)
  }

  test("boats valid true") {
    val boarData = new BoardData(2, new Random(), AnimalInfo.importFromFile().get)
    val boardUtils = new BoardUtils(boarData)
    val deployProcessor = new DeploymentProcessor(boardUtils)

    val boats = List(
      Boat(Hex(1, HexType.CLAY), Side.north),
      Boat(Hex(2, HexType.CLAY), Side.north),
      Boat(Hex(3, HexType.CLAY), Side.north)
    )

    val result = deployProcessor.valid(boats)
    assert(result._1)
  }
}
