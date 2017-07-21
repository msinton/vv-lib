package com.consideredgames.game.model.person.tools

import com.consideredgames.game.model.exceptions.ResourceInvalidException
import com.consideredgames.game.model.info.person.skill.Skills._
import org.scalatest.FunSuite

class ToolsTest extends FunSuite {

  test("that imports and sets up mappings correctly") {

    val tools = Tools("/5Tools.txt")

    assert(tools.tools.length === 5)
  }

  test("that throws exception when file does not exist") {

    val e = intercept[ResourceInvalidException] {
      Tools("does not exist")
    }
    assert(e !== null)
  }

  test("that throws exception when bad file") {

    val e = intercept[ResourceInvalidException] {
      Tools("/toolsBad.txt")
    }
    assert(e !== null)
  }

  test("that core setup correctly") {

    val tools = Tools("/5Tools.txt")

    assert(tools.core(Attacker).size === 4)
    assert(tools.core(Lumberjack).size === 4)
    assert(tools.core.get(Carpenter) === None)
  }

  test("that bonuses setup correctly") {

    val tools = Tools("/5Tools.txt")

    assert(tools.bonuses(Attacker).size === 4)
    assert(tools.bonuses(Lumberjack).size === 4)
    assert(tools.bonuses(Carpenter).size === 4)
  }

  test("that can get tools that make the tool") {

    val tools = Tools("/5Tools.txt")

    assert(tools.tools(0).madeFromTools.size === 0)
    assert(tools.tools(1).madeFromTools === Map(tools.toolsByName("sticks") -> 1))
    assert(tools.tools(2).madeFromTools === Map(tools.toolsByName("sticks") -> 1))
    assert(tools.tools(3).madeFromTools === Map(tools.toolsByName("sticks") -> 2))
  }

  test("that can get tools that make the tool 2") {

    val tools = Tools()

    println(tools.toolsByName("stone axe"))
    assert(tools.toolsByName("stone axe").madeFromTools === Map(tools.toolsByName("wooden rod") -> 1))
  }

  test("that tools are sorted") {
    val tools = Tools("/5Tools.txt")

    assert(tools.tools(0).name == "sticks")
    assert(tools.tools(0).worth === 0)
    assert(tools.tools(1).name == "fine stone axe")
    assert(tools.tools(1).worth === 1)
    assert(tools.tools(2).name == "stone axe")
    assert(tools.tools(2).worth === 1)
  }
}

