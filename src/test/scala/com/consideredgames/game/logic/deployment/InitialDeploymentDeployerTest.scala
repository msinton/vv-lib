package com.consideredgames.game.logic.deployment

import com.consideredgames.game.model.animals.AnimalInfo
import com.consideredgames.game.model.board.{BoardData, BoardUtils}
import com.consideredgames.game.model.person.Person
import com.consideredgames.game.model.person.tools.{ToolUtils, Tools}
import com.consideredgames.game.model.player.FullPlayer
import com.consideredgames.game.model.player.PlayerColours._
import com.consideredgames.game.model.resources.ItemContainer
import com.consideredgames.message.DeployedPerson
import org.scalatest.{FunSuite, OptionValues}

import scala.util.Random

/**
 * Created by matt on 05/10/15.
 */
class InitialDeploymentDeployerTest extends FunSuite with OptionValues {

  val toolUtils = ToolUtils(new Tools())

  val submitHandle = {ppl: List[DeployedPerson] => }

  test("the whole shebang") {
    val boardData = new BoardData(2, new Random(1), AnimalInfo.importFromFile().get)
    val deploymentProcessor = DeploymentProcessor(BoardUtils(boardData))
    val player = FullPlayer("Bob", new ItemContainer(toolUtils), Black)
    val people = List(
      Person(1, Black),
      Person(2, Black),
      Person(3, Black),
      Person(4, Black),
      Person(5, Black))
    val state = InitialDeploymentDeployer(player, people, deploymentProcessor, submitHandle, firstPlayer = true)

    assert(state.place(people(0), boardData.getHex(1).value))
    assert(state.place(people(1), boardData.getHex(2).value))
    assert(state.place(people(2), boardData.getHex(3).value))
    assert(state.place(people(3), boardData.getHex(4).value))
    assert(state.place(people(4), boardData.getHex(5).value))

    assert(state.finished())

    assert(state.toPlace.isEmpty)

    state.undo(boardData.getHex(1).value)

    assert(state.toPlace == Set(Person(1, Black)))

    assert(!state.finished())

    //Next player
    val player2 = FullPlayer("Fred", new ItemContainer(toolUtils), Blue)
    val people2 = List(
      Person(1, Blue),
      Person(2, Blue),
      Person(3, Blue),
      Person(4, Blue),
      Person(5, Blue))
    val state2 = InitialDeploymentDeployer(player2, people2, deploymentProcessor, submitHandle, firstPlayer = false)

    assert(!state2.isValid(people(0), boardData.getHex(1).value))

    assert(!state2.isValid(people(0), boardData.getHex(10).value))

    assert(state2.place(people(0), boardData.getHex(31).value))
    assert(state2.place(people(1), boardData.getHex(32).value))
    assert(state2.place(people(2), boardData.getHex(33).value))
    assert(state2.place(people(3), boardData.getHex(40).value))

    assert(state2.toPlace == Set(people(4)))
    assert(state2.place(people(4), boardData.getHex(45).value))
    assert(state2.toPlace == Set())

    assert(state2.finished())
  }
}
