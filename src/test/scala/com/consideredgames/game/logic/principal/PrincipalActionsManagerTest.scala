package com.consideredgames.game.logic.principal

import com.consideredgames.game.model.animals.AnimalInfo
import com.consideredgames.game.model.info.person.skill.Skills.Carpenter
import com.consideredgames.game.model.person.Person
import com.consideredgames.game.model.person.tools.{Tool, ToolUtils, Tools}
import com.consideredgames.game.model.player.FullPlayer
import com.consideredgames.game.model.resources.ItemContainer
import com.consideredgames.game.model.round.principal.Actions
import org.scalatest.{FunSuite, PrivateMethodTester}

class PrincipalActionsManagerTest extends FunSuite with PrivateMethodTester {

  val tools = Tools("/5Tools.txt")

  val toolUtils = ToolUtils(tools)

  test("degrade tools when life is above 1") {

    val itemContainer = new ItemContainer(toolUtils)

    val degradeToolsMethod = PrivateMethod[Unit]('degradeUsedTools)

    val target = new PrincipalActionsManager()

    object personMock extends Person(1)

    // assign items to person
    val tool = tools.toolsByName("stone axe")
    itemContainer.add(tool, Tool(2))
    itemContainer.assign(tool, personMock)

    target invokePrivate degradeToolsMethod(personMock, Carpenter, toolUtils, itemContainer)

    assert(itemContainer.assignedTools((tool, personMock.id)) === Tool(1))
    assert(itemContainer.assignedTools.size === 1)
  }

  test("degrade tools when life is 1") {

    val itemContainer = new ItemContainer(toolUtils)

    val degradeToolsMethod = PrivateMethod[Unit]('degradeUsedTools)

    val target = new PrincipalActionsManager()

    object personMock extends Person(1)

    // assign items to person
    val tool = tools.toolsByName("stone axe")
    itemContainer.add(tool, Tool(1))
    itemContainer.assign(tool, personMock)

    target invokePrivate degradeToolsMethod(personMock, Carpenter, toolUtils, itemContainer)

    assert(itemContainer.assignedTools.get((tool, personMock.id)) === None)
    assert(itemContainer.assignedTools.size === 0)
  }

  test("add action") {

    val target = new PrincipalActionsManager(List(), 4)

    val actions = new Actions(toolUtils, AnimalInfo.importFromFile().get)

    object personMock extends Person(1)
    object personMock2 extends Person(2)
    object personMock3 extends Person(3)
    object personMock4 extends Person(4)

    target.addAction(actions.MineClay, personMock)
    assert(target.actionPoints === 3)
    target.addAction(actions.Fish, personMock)
    assert(target.actionPoints === 3)
    target.addAction(actions.MineClay, personMock2)
    assert(target.actionPoints === 2)
    target.addAction(actions.Fish, personMock3)
    assert(target.actionPoints === 1)
    target.addAction(actions.Fish, personMock4)
    assert(target.actionPoints === 0)
    target.addAction(actions.Fish, personMock)
    assert(target.actionPoints === 0)

    val itemContainer = new ItemContainer(toolUtils)
    object player extends FullPlayer("name", itemContainer, null)
    target.process(player)
  }
}