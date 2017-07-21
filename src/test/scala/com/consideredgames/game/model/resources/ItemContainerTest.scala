package com.consideredgames.game.model.resources

import com.consideredgames.game.model.person.Person
import com.consideredgames.game.model.person.tools.{Tool, ToolUtils, Tools}
import com.consideredgames.game.model.resources.Resources.{Coal, Food}
import com.consideredgames.game.model.player.PlayerColours.Black
import org.scalatest.{FunSuite, OptionValues}


class ItemContainerTest extends FunSuite with OptionValues {

  val tools = new Tools()
  val toolUtils = ToolUtils(tools)

  test("that can add and remove resources ok") {

    val c = ItemContainer(toolUtils)

    c.add(ResourceGroup(Coal, 2))
    c.add(ResourceGroup(Coal, 1))

    assert(c.resources.find(e => e.r == Coal).value.n === 3)

    c.consume(Coal, 1)

    assert(c.resources.find(e => e.r == Coal).value.n === 2)

    assert(c.consume(Coal, 2))
    assert(!c.consume(Coal, 1))
  }

  test("that can add and remove food ok") {

    val c = ItemContainer(toolUtils)

    c.add(ResourceGroup(Food, 2))
    c.add(ResourceGroup(Food, 4))

    assert(c.resources.find(e => e.r == Food).value.n === 6)

    c.consume(Food, 1)

    assert(c.resources.find(e => e.r == Food).value.n === 5)

    assert(c.consume(Food, 5))
    assert(!c.consume(Food, 1))
  }

  test("that can add and remove tools ok") {

    val c = ItemContainer(toolUtils)

    val tInfo = tools.tools.head

    c.add(tInfo, Tool(1))
    c.add(tInfo, Tool(2))

    assert(c.tools.get(tInfo).value.contains(Tool(1)))
    assert(c.tools.get(tInfo).value.contains(Tool(2)))
    assert(c.tools.get(tInfo).value.size === 2)

    assert(c.take(tInfo, Tool(1)).value === Tool(1))

    assert(c.tools.get(tInfo).value.contains(Tool(2)))
    assert(c.tools.get(tInfo).value.size === 1)

    c.take(tInfo, Tool(2))

    assert(c.tools.get(tInfo).value.size === 0)

    assert(c.take(tInfo, Tool(1)) === None)
  }

  test("that tools are ordered ok") {

    val tools = Tools("/5Tools.txt")
    val toolUtils = ToolUtils(tools)

    val c = ItemContainer(toolUtils)

    val tInfo1 = tools.tools(0)
    val tInfo2 = tools.tools(1)
    val tInfo3 = tools.tools(2)
    val tInfo4 = tools.tools(3)

    c.add(tInfo1, Tool(1))
    c.add(tInfo2, Tool(1))
    c.add(tInfo3, Tool(1))
    c.add(tInfo4, Tool(1))

    assert(c.tools.keys.size === 4)
    val itr = c.tools.keys.iterator
    assert(itr.next() === tools.toolsByName("sticks"))
    assert(itr.next() === tools.toolsByName("fine stone axe"))
    assert(itr.next() === tools.toolsByName("stone axe"))
    assert(itr.next() === tools.toolsByName("iron axe"))
  }

  test("that assignTool does not swap tools") {

    val c = ItemContainer(toolUtils)

    val p = Person(1, Black)

    val tInfo1 = tools.tools(0)
    val tInfo2 = tools.tools(1)

    c.add(tInfo1, Tool(1))
    c.add(tInfo2, Tool(3))

    c.assign(tInfo1, p)

    assert(c.tools.get(tInfo1).value.isEmpty) // tool is not available
    assert(c.assignedTools.get((tInfo1, p.id)).value.life === 1) // has been assigned
    assert(p.tools.contains(tInfo1) && p.tools.size === 1)

    c.add(tInfo1, Tool(2))
    // assign same type
    c.assign(tInfo1, p)

    assert(c.assignedTools.get((tInfo1, p.id)).value.life === 1)
    assert(c.tools.get(tInfo1).value(0).life == 2)
  }


}