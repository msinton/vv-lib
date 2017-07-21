package com.consideredgames.game.model.round.principal

import com.consideredgames.game.model.animals._
import com.consideredgames.game.model.hex._
import com.consideredgames.game.model.info.person.skill.Skills._
import com.consideredgames.game.model.person.Person
import com.consideredgames.game.model.person.tools._
import com.consideredgames.game.model.player.PlayerColours._
import com.consideredgames.game.model.resources.Resources._
import com.consideredgames.game.model.resources._
import com.consideredgames.game.model.round.principal._
import org.json4s.DefaultFormats
import org.json4s.native.Serialization._
import org.scalatest.{FunSuite, OptionValues}

/**
 * Created by matt on 16/04/15.
 */
class ActionsTest extends FunSuite with OptionValues {
  val tools = new Tools()
  val toolUtils = new ToolUtils(tools)
  val animalInfos = AnimalInfo.importFromFile().get
  val actions = new Actions(toolUtils, animalInfos)
  val itemContainer = new ItemContainer(toolUtils)

  object riverMock extends RiverSegment(null, null) {
    override def addToHexes(sideA: Side) = {}
  }

  object hexStone extends Hex(3) {
    override def hexType = HexType.STONE
  }

  object hexWood extends Hex(3) {
    override def hexType = HexType.WOODS
  }

  object hexPlains extends Hex(3) {
    override def hexType = HexType.PLAINS
  }

  object hexClayByWater extends Hex(1) {
    override def rivers = collection.mutable.Map(Side.north -> riverMock)
    override def hexType = HexType.CLAY
  }

  object hexStoneByWater extends Hex(2) {
    override def rivers = collection.mutable.Map(Side.north -> riverMock)
    override def hexType = HexType.STONE
  }

  val horseInfo = animalInfos.find(_.name == "horse").value

  val hexWithHorse = Hex(5, HexType.CLAY)
  val an = new AnimalManager(animalInfos, Left(hexWithHorse))
  an.add(Animal(horseInfo, true))
  hexWithHorse.animalManager = Some(an)

  val personByWaterOnClay = Person(1, Black)
  personByWaterOnClay.hex = Some(hexClayByWater)

  val personOnStone = Person(2, Black)
  personOnStone.hex = Some(hexStone)

  //for repro
  val personBlackNoTools = Person(2, Black)
  val personBlack = Person(2, Black)

  object hex2 extends Hex(1)
  hex2.person = Some(personBlack)

  object hexWithBlackPersonNeighbour extends Hex(2) {
    override def neighboursAccessibleByFoot() = collection.immutable.Map(Side.north -> hex2)
  }


  test("fish without tools") {

    val action = actions.Fish

    assert(!action.isPossible(personByWaterOnClay))
    assert(action.result(personByWaterOnClay) === EmptyResult)

    assert(!action.isPossible(person = personByWaterOnClay, itemContainer)._1)
    assert(action.isPossible(person = personByWaterOnClay, itemContainer)._2 === Set())
  }

  test("fish with no water nearby") {

    val personByWater = Person(1, Black)
    personByWater.hex = Some(hexStone)

    personByWater.tools += tools.toolsByName("fishing rod")

    val action = actions.Fish

    assert(!action.isPossible(personByWater))
    assert(action.result(personByWater) == EmptyResult)
  }

  test("fish") {

    val personByWater = Person(1, Black)
    personByWater.hex = Some(hexClayByWater)

    val action = actions.Fish

    //person without tool - tool in container
    val rod = tools.toolsByName("fishing rod")
    itemContainer.add(rod, Tool(1))

    assert(action.isPossible(person = personByWater, itemContainer)._1)
    assert(action.isPossible(person = personByWater, itemContainer)._2 === Set(rod))

    personByWater.tools += rod

    assert(action.isPossible(personByWater))
    assert(action.result(personByWater) == ResourceResult(ResourceProduct(List(ResourceGroup(Food, 2))), 1, Fisherman))
  }

  test("mine without tools") {
    val action = actions.MineStone

    assert(!action.isPossible(personOnStone))
    assert(action.result(personOnStone) === EmptyResult)
  }

  test("mine") {

    val personOnStoneWithTools = Person(2, Black)
    personOnStoneWithTools.hex = Some(hexStone)
    personOnStoneWithTools.tools += tools.toolsByName("fine stone pickaxe")

    val action = actions.MineStone

    assert(action.isPossible(personOnStoneWithTools))
    assert(action.result(personOnStoneWithTools) == ResourceResult(ResourceProduct(List(ResourceGroup(Stone, 2))), 1, Miner))
  }

  test("mine on plains") {

    val personOnPlainsWithTools = Person(2, Black)
    personOnPlainsWithTools.hex = Some(hexPlains)
    personOnPlainsWithTools.tools += tools.toolsByName("fine stone pickaxe")

    val action = actions.MineUnknown

    assert(action.isPossible(personOnPlainsWithTools))
    action.foundType(HexType.STONE)
    assert(action.result(personOnPlainsWithTools) == ResourceResult(ResourceProduct(List(ResourceGroup(Stone, 2))), 1, Miner))
  }

  test("mine on woods") {

    val personOnWoodsWithTools = Person(2, Black)
    personOnWoodsWithTools.hex = Some(hexWood)
    personOnWoodsWithTools.tools += tools.toolsByName("fine stone pickaxe")

    val action = actions.MineClay

    assert(!action.isPossible(personOnWoodsWithTools))
    assert(action.result(personOnWoodsWithTools) == EmptyResult)

    val action2 = actions.MineOre

    assert(!action2.isPossible(personOnWoodsWithTools))
    assert(action2.result(personOnWoodsWithTools) == EmptyResult)
  }

  test("reproduce no partner") {

    val personNoTools = Person(2, Black)

    val action = actions.Reproduce

    assert(!action.isPossible(personNoTools))
  }

  test("reproduce person specified not a neighbour") {

    val personBlackNoTools = Person(1, Black)
    val personBlack = Person(2, Black)
    val person3 = Person(3, Black)

    val hex2 = Hex(1, HexType.CLAY)
    hex2.person = Some(personBlack)

    val hexWithBlackPersonNeighbour = Hex(2, HexType.CLAY)
    hexWithBlackPersonNeighbour.neighbours.update(Side.north, hex2)

    personBlackNoTools.hex = Some(hexWithBlackPersonNeighbour)

    val action = actions.Reproduce

    assert(!action.isPossible(personBlackNoTools, person3))
    assert(action.result(personBlackNoTools, person3) == EmptyResult)
  }

  test("reproduce person specified is a neighbour") {

    personBlackNoTools.hex = Some(hexWithBlackPersonNeighbour)

    val action = actions.Reproduce

    assert(action.isPossible(personBlackNoTools, personBlack))
    assert(action.result(personBlackNoTools, personBlack) == PersonProductionResult(70,personBlack, 1, Reproducer))
  }

  test("hunt horse") {

    object person extends Person(1)
    person.hex = Some(hexWithHorse)
    person.tools += tools.toolsByName("fine stone axe")

    val action = actions.HuntHorse

    assert(action.isPossible(person))
    assert(action.result(person) == HuntingResult(List(54, 34), animalInfos.find(_.name == "horse").get, 1, Attacker))
  }

  test("serialize") {

    implicit val formats = DefaultFormats + actions.serializer

    val action = actions.Fish

    val json = write(action)

    val obj = read[Action](json)

    assert(obj == action)
  }
}
