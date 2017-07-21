package com.consideredgames.game.model.person.tools

import com.consideredgames.game.model.info.person.skill.Skills._
import com.consideredgames.game.model.person.Person
import com.consideredgames.game.model.player.PlayerColours._
import com.consideredgames.game.model.resources.ItemContainer
import org.scalatest.{FunSuite, OptionValues}

/**
 * Created by matt on 16/03/15.
 */
class ToolUtilsTest extends FunSuite with OptionValues {

  val tools = new Tools()

  val toolUtils = new ToolUtils(tools)

  test ("auto assign tools when have none") {
    val c = new ItemContainer(toolUtils)

    val p = Person(1, playerColour = Black)

    toolUtils.autoAssignBestAvailableTools(c, p, Attacker)
    assert(p.tools.isEmpty)
  }

  test("best bonus gets the tool with the best bonus for the skill") {

    val c = ItemContainer(toolUtils)

    val bestWeapons = tools.toolsByName.get("fine iron sword").value :: tools.toolsByName.get("fine iron axe").value :: Nil

    val attackerTools = tools.toolsByName.get("iron axe").value :: tools.toolsByName.get("stone sledgehammer").value ::
      tools.toolsByName.get("iron sword").value :: tools.toolsByName.get("fine iron sword").value ::
      tools.toolsByName.get("stone sword").value :: tools.toolsByName.get("stone knife").value ::
      tools.toolsByName.get("fine iron knife").value :: tools.toolsByName.get("iron knife").value ::
      tools.toolsByName.get("diamond edged knife").value :: tools.toolsByName.get("bone knife").value ::
      tools.toolsByName.get("stone axe").value :: tools.toolsByName.get("stone sledgehammer").value ::
      tools.toolsByName.get("fine iron axe").value :: Nil

    for (tl <- attackerTools) {
      c.add(tl, Tool(1))
    }

    val bestTool = toolUtils.bestBonus(attackerTools, Attacker)

    assert(bestWeapons.contains(bestTool))
  }

  test ("auto assign gives best tools and works with interchangeables") {

    val c = ItemContainer(toolUtils)

    val p = Person(1, playerColour = Black)

    val bestWeapons = tools.toolsByName.get("fine iron sword").value :: tools.toolsByName.get("fine iron axe").value :: Nil

    val attackerTools = tools.toolsByName.get("iron axe").value :: tools.toolsByName.get("stone sledgehammer").value ::
      tools.toolsByName.get("iron sword").value :: tools.toolsByName.get("fine iron sword").value ::
      tools.toolsByName.get("stone sword").value :: tools.toolsByName.get("stone knife").value ::
      tools.toolsByName.get("fine iron knife").value :: tools.toolsByName.get("iron knife").value ::
      tools.toolsByName.get("diamond edged knife").value :: tools.toolsByName.get("bone knife").value ::
      tools.toolsByName.get("stone axe").value :: tools.toolsByName.get("stone sledgehammer").value ::
      tools.toolsByName.get("fine iron axe").value :: Nil

    for (tl <- attackerTools) {
      c.add(tl, Tool(1))
    }

    toolUtils.autoAssignBestAvailableTools(c, p, Attacker)

    assert(p.tools.intersect(bestWeapons.toSet).size === 1)
    println(p.tools)
    assert(p.tools.size === 1)
  }

  test ("auto assign as expected when no core to assign") {

    //no core tools: Tailor, farmers, Cook, Reprod

    val c = ItemContainer(toolUtils)

    val p = Person(1, playerColour =  Black)

    val bestTools = tools.toolsByName.get("bed").value :: tools.toolsByName.get("feather pillow").value :: Nil

    val reproducerTools = tools.toolsByName.get("cushion").value ::
      tools.toolsByName.get("hide mat").value :: bestTools

    for (tl <- reproducerTools) {
      c.add(tl, Tool(1))
    }

    toolUtils.autoAssignBestAvailableTools(c, p, Reproducer)

    assert(p.tools.intersect(bestTools.toSet).size === 2)
    assert(p.tools.size === 2)
  }

  test ("auto assign works when already have a core tool assigned") {

    val c = ItemContainer(toolUtils)

    val p = Person(1, playerColour =  Black)

    val bestWeapons = tools.toolsByName.get("fine iron sword").value :: tools.toolsByName.get("fine iron axe").value :: Nil

    val attackerTools = tools.toolsByName.get("iron axe").value :: tools.toolsByName.get("stone sledgehammer").value ::
      tools.toolsByName.get("iron sword").value :: tools.toolsByName.get("fine iron sword").value ::
      tools.toolsByName.get("stone sword").value :: tools.toolsByName.get("stone knife").value ::
      tools.toolsByName.get("fine iron knife").value :: tools.toolsByName.get("iron knife").value ::
      tools.toolsByName.get("diamond edged knife").value :: tools.toolsByName.get("bone knife").value ::
      tools.toolsByName.get("stone axe").value :: tools.toolsByName.get("stone sledgehammer").value ::
      tools.toolsByName.get("fine iron axe").value :: Nil

    for (tl <- attackerTools) {
      c.add(tl, Tool(1))
    }

    // already has a core tool
    assert(c.assign(tools.toolsByName.get("iron axe").value, p))

    toolUtils.autoAssignBestAvailableTools(c, p, Attacker)

    assert(p.tools.intersect(bestWeapons.toSet).size === 1)
    assert(p.tools.size === 2) // does not lose the tool he had
  }

  test ("that getSkillBonus should take interchanges into account - so don't get higher bonus than should, and uses the best one") {
    val c = ItemContainer(toolUtils)

    val p = Person(1, playerColour =  Black)

    val attackerTools = tools.toolsByName.get("iron axe").value :: tools.toolsByName.get("stone sledgehammer").value ::
      tools.toolsByName.get("iron sword").value :: tools.toolsByName.get("fine iron sword").value ::
      tools.toolsByName.get("stone sword").value :: tools.toolsByName.get("stone knife").value ::
      tools.toolsByName.get("fine iron knife").value :: tools.toolsByName.get("iron knife").value ::
      tools.toolsByName.get("diamond edged knife").value :: tools.toolsByName.get("bone knife").value ::
      tools.toolsByName.get("stone axe").value :: tools.toolsByName.get("stone sledgehammer").value ::
      tools.toolsByName.get("fine iron axe").value :: Nil

    for (tl <- attackerTools) {
      c.add(tl, Tool(1))
      c.assign(tl, p)
    }

    assert(toolUtils.getSkillBonus(p, Attacker) === 4)
  }

  test ("that getSkillBonus ok - mason") {
    val c = ItemContainer(toolUtils)

    val p = Person(1, playerColour =  Black)

    val masonTools = tools.toolsByName.get("hide apron").value :: tools.toolsByName.get("stone sledgehammer").value ::
      tools.toolsByName.get("iron chisel").value :: tools.toolsByName.get("stone chisel").value ::
      tools.toolsByName.get("iron hammer").value :: tools.toolsByName.get("stone hammer").value ::
      tools.toolsByName.get("fine stone hammer").value :: tools.toolsByName.get("wooden mallet").value :: Nil

    for (tl <- masonTools) {
      c.add(tl, Tool(1))
      c.assign(tl, p)
    }

    println(toolUtils.filterOutInterchangeablesChooseBest(masonTools.toSet, Mason))
    assert(toolUtils.getSkillBonus(p, Mason) === 5)
  }

  test("canMeetCoreRequirement") {

    val p = Person(1, playerColour =  Black)

    val masonTools = Set(tools.toolsByName.get("hide apron").value, tools.toolsByName.get("stone sledgehammer").value,
      tools.toolsByName.get("iron chisel").value, tools.toolsByName.get("stone chisel").value,
      tools.toolsByName.get("iron hammer").value, tools.toolsByName.get("stone hammer").value,
      tools.toolsByName.get("fine stone hammer").value, tools.toolsByName.get("wooden mallet").value)

    assert(toolUtils.canMeetCoreRequirement(p, Mason, Set()) === (false, Set()) )

    assert(toolUtils.canMeetCoreRequirement(p, Mason, masonTools) === (true, Set(tools.toolsByName("stone chisel"), tools.toolsByName("iron chisel"))))

    p.tools += tools.toolsByName("stone chisel")
    assert(toolUtils.canMeetCoreRequirement(p, Mason, Set()) === (true, Set()))
  }

  //TODO to go into PrincipalActionManager
  // submit
  test ("on submit that tools degrade") {

  }
  //submit
  test ("that replenishes the people tools that were degraded where possible - for the moment does autoAssign on the skill that was used") {

  }
  // end of round processor
  test("that killAnimalsThatExceedCapacity is called") {

  }

}
