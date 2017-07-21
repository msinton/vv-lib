package integration

import com.consideredgames.game.logic.principal.PrincipalActionsManager
import com.consideredgames.game.model.animals._
import com.consideredgames.game.model.hex._
import com.consideredgames.game.model.person.Person
import com.consideredgames.game.model.person.tools._
import com.consideredgames.game.model.player.PlayerColours._
import com.consideredgames.game.model.resources.ItemContainer
import com.consideredgames.game.model.round.principal.Actions
import org.scalatest.{FunSuite, OptionValues}

/**
 * Created by matt on 09/07/15.
 */
class ProcessingActionsBetweenServerAndClientTest extends FunSuite  with OptionValues {

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

  test("client submits fish action, server receives then processes then sends results back, client completes processing") {

    val principalActionManager = PrincipalActionsManager(List(), 1)

    val personByWater = Person(1, Black)
    personByWater.hex = Some(hexClayByWater)

    val action = actions.Fish

    //person without tool - tool in container
    val rod = tools.toolsByName("fishing rod")
    itemContainer.add(rod, Tool(1))

    personByWater.tools += rod

    principalActionManager.addAction(action, personByWater)

    val submits = principalActionManager.actions

//    val intoSubmitable = submits map {
//      _.actionParameter match {
//        case x: PersonActionParameter => x.person.tools
//      }
//    }
    // send actions to server
    // other player sends actions
    // TODO server has all players actions so proceeds
    // server locates persons
    // server processes actions - updating its data
    // sends results back to clients
    // clients complete their processing

//    principalActionManager.process()

    //
  }
}
