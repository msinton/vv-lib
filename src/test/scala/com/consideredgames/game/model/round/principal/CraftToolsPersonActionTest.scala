package com.consideredgames.game.model.round.principal

import com.consideredgames.game.model.info.person.skill.SkillStat
import com.consideredgames.game.model.info.person.skill.Skills.Mason
import com.consideredgames.game.model.person.Person
import com.consideredgames.game.model.person.tools._
import com.consideredgames.game.model.resources.Resources._
import com.consideredgames.game.model.resources._
import com.consideredgames.game.model.round.principal._
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
 * Created by matt on 02/05/15.
 */
class CraftToolsPersonActionTest extends FunSuite with BeforeAndAfter {

  val tools = new Tools()
  val toolUtils = new ToolUtils(tools)
  var itemContainer: ItemContainer = _
  var person: Person = _

  before {
    itemContainer = new ItemContainer(toolUtils)
    person = Person(1, null)
  }

  // mason crafter
  object CraftToolsPersonActionImpl extends CraftToolsPersonAction("name", Mason, SimpleToolProductFormula(List(1,2,3,4)), toolUtils)

  test("meetsActionRequirements fails when no resources") {

    val order = List(tools.toolsByName("stone axe"))

    assert(!CraftToolsPersonActionImpl.meetsActionRequirements(person, order, itemContainer))
  }

  test("meetsActionRequirements with resources but not tool required") {

    val order = List(tools.toolsByName("stone axe"))
    itemContainer.add(ResourceGroup(Stone, 1))

    assert(!CraftToolsPersonActionImpl.meetsActionRequirements(person, order, itemContainer))
    assert(itemContainer.resources == Seq(ResourceGroup(Stone, 1)))
  }

  test("meetsActionRequirements with tool required but not resources") {

    val order = List(tools.toolsByName("stone axe"))
    itemContainer.add(tools.toolsByName("wooden rod"), Tool(1))

    assert(!CraftToolsPersonActionImpl.meetsActionRequirements(person, order, itemContainer))
    assert(itemContainer.tools == Map(tools.toolsByName("wooden rod") -> Seq(Tool(1))))
  }

  test("meetsActionRequirements with tool required and resources") {

    val order = List(tools.toolsByName("stone axe"))
    itemContainer.add(ResourceGroup(Stone, 1))
    itemContainer.add(tools.toolsByName("wooden rod"), Tool(1))

    assert(CraftToolsPersonActionImpl.meetsActionRequirements(person, order, itemContainer))
    assert(itemContainer.resources == Seq(ResourceGroup(Stone, 0)))
    assert(itemContainer.tools == Map(tools.toolsByName("wooden rod") -> Seq()))
  }

  test("meetsActionRequirements false when order has item skill cant build") {

    val order = List(tools.toolsByName("iron axe"))
    itemContainer.add(tools.toolsByName("wooden rod"), Tool(1))
    itemContainer.add(ResourceGroup(IronOre, 1))

    assert(!CraftToolsPersonActionImpl.meetsActionRequirements(person, order, itemContainer))
    assert(itemContainer.tools == Map(tools.toolsByName("wooden rod") -> Seq(Tool(1))))
    assert(itemContainer.resources == Seq(ResourceGroup(IronOre, 1)))
  }

  test("meetsActionRequirements false when not skilled enough") {

    val order = List(tools.toolsByName("stone axe"), tools.toolsByName("fine stone axe")) //fine stone axe requires level 3
    itemContainer.add(tools.toolsByName("wooden rod"), Tool(1))
    itemContainer.add(tools.toolsByName("wooden rod"), Tool(1))
    itemContainer.add(ResourceGroup(Stone, 3))

    assert(!CraftToolsPersonActionImpl.meetsActionRequirements(person, order, itemContainer))
    assert(itemContainer.tools == Map(tools.toolsByName("wooden rod") -> Seq(Tool(1), Tool(1))))
    assert(itemContainer.resources == Seq(ResourceGroup(Stone, 3)))

    //increase skill level, then can do it
    itemContainer.add(tools.toolsByName("stone chisel"), Tool(1))
    itemContainer.assign(tools.toolsByName("stone chisel"), person) // bonus of 1 and a core tool
    person.skills.+=(Mason, SkillStat(2))

    assert(CraftToolsPersonActionImpl.meetsActionRequirements(person, order, itemContainer))
    assert(itemContainer.tools == Map(tools.toolsByName("wooden rod") -> Seq(), tools.toolsByName("stone chisel") -> Seq()))
    assert(itemContainer.resources == Seq(ResourceGroup(Stone, 0)))
  }

  test("meetsActionRequirements false when too many items in order") {

    itemContainer.add(tools.toolsByName("stone chisel"), Tool(1))
    itemContainer.assign(tools.toolsByName("stone chisel"), person) // bonus of 1 and a core tool
    person.skills.+=(Mason, SkillStat(1))

    val order = ((1 to 4) map (_ => tools.toolsByName("stone axe"))).toList
    for (i <- 1 to 4) {
      itemContainer.add(tools.toolsByName("wooden rod"), Tool(1))
      itemContainer.add(ResourceGroup(Stone, 1))
    }

    assert(!CraftToolsPersonActionImpl.meetsActionRequirements(person, order, itemContainer))
    assert(itemContainer.tools == Map(tools.toolsByName("wooden rod") -> Seq(Tool(1), Tool(1), Tool(1), Tool(1)), tools.toolsByName("stone chisel") -> Seq()))
    assert(itemContainer.resources == Seq(ResourceGroup(Stone, 4)))

    // works when 3 items in order
    assert(CraftToolsPersonActionImpl.meetsActionRequirements(person, order.drop(1), itemContainer))
    assert(itemContainer.tools == Map(tools.toolsByName("wooden rod") -> Seq(Tool(1)), tools.toolsByName("stone chisel") -> Seq()))
    assert(itemContainer.resources == Seq(ResourceGroup(Stone, 1)))
  }

}
