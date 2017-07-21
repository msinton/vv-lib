package com.consideredgames.game.model.animals

import com.consideredgames.game.model.person.tools.{ToolUtils, Tools}
import com.consideredgames.game.model.resources.{ItemContainer, ResourceGroup}
import com.consideredgames.game.model.resources.Resources._
import org.scalatest.FunSuite

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/**
 * Created by matt on 09/03/15.
 */
class AnimalHarvesterTest extends FunSuite {

  val anims = AnimalInfo.importFromFile()
  val toolUtils = new ToolUtils(new Tools)

  test("harvest ok simple case") {

    val itemContainer = new ItemContainer(toolUtils)

    val container = new AnimalContainer(1)
    val a = Animal(anims.get(0), true)
    container.add(a)

    assert(AnimalHarvester.harvest(container, 1, itemContainer) === List(a))
    assert(itemContainer.resources.find(e => e.r == Food).get.n === 4)
    assert(itemContainer.resources.find { e => e.r == Hide}.get.n === 3)
  }

  test("harvest ok many, horse") {

    val itemContainer = new ItemContainer(toolUtils)

    val container = new AnimalContainer(3)
    val a = Animal(anims.get(0), true)
    val b = Animal(anims.get(0), true)
    val c = Animal(anims.get(0), true)
    container.add(a)
    container.add(b)
    container.add(c)

    assert(AnimalHarvester.harvest(container, 3, itemContainer) === List(c,b,a))
    assert(itemContainer.resources.find(e => e.r == Food).get.n === 12)
    assert(itemContainer.resources.find(e => e.r == Hide).get.n === 9)
  }

  test("harvest ok many, boar") {

    val itemContainer = new ItemContainer(toolUtils)

    val container = new AnimalContainer(3)
    val a = Animal(anims.get(1), true)
    val b = Animal(anims.get(1), true)
    val c = Animal(anims.get(1), true)
    container.add(a)
    container.add(b)
    container.add(c)

    assert(AnimalHarvester.harvest(container, 3, itemContainer) === List(c,b,a))
    assert(itemContainer.resources.find(e => e.r == Food).get.n === 9)
    assert(itemContainer.resources.find { e => e.r == Hide}.get.n === 6)
  }

  test("harvest ok many, chicken") {

    val itemContainer = new ItemContainer(toolUtils)

    val container = new AnimalContainer(3)
    val a = Animal(anims.get(2), true)
    val b = Animal(anims.get(2), true)
    val c = Animal(anims.get(2), true)
    container.add(a)
    container.add(b)
    container.add(c)

    assert(AnimalHarvester.harvest(container, 2, itemContainer) === List(b,a))
    assert(itemContainer.resources.find(e => e.r == Food).get.n === 1)
    assert(itemContainer.resources.find { e => e.r == Hide} === None)
    assert(itemContainer.resources.find { e => e.r == Feathers}.get.n === 1)
  }

  test("invalid cases return false") {

    val itemContainer = new ItemContainer(toolUtils)

    val container = new AnimalContainer(3)
    val a = Animal(anims.get(2), true)
    val b = Animal(anims.get(2), true)
    val c = Animal(anims.get(2), true)
    container.add(a)
    container.add(b)
    container.add(c)

    assert(!AnimalHarvester.validHarvestRequest(container, 1))
    assert(!AnimalHarvester.validHarvestRequest(container, 3))
    assert(!AnimalHarvester.validHarvestRequest(container, 4))

    assert(AnimalHarvester.harvest(container, 3, itemContainer) === List())
  }

  test("hunt 1 chicken") {

    val itemContainer = new ItemContainer(toolUtils)
    val container = new AnimalContainer(3)
    val a = Animal(anims.get(2), true)
    val b = Animal(anims.get(2), true)
    val c = Animal(anims.get(2), true)
    container.add(a)
    container.add(b)
    container.add(c)

    AnimalHarvester.hunt(container, 1, itemContainer, new Random(1))

    assert(itemContainer.resources === ArrayBuffer())
    assert(container.size === 2)
  }

  test("hunt 2 chickens") {

    val itemContainer = new ItemContainer(toolUtils)
    val container = new AnimalContainer(3)
    val a = Animal(anims.get(2), true)
    val b = Animal(anims.get(2), true)
    val c = Animal(anims.get(2), true)
    container.add(a)
    container.add(b)
    container.add(c)

    AnimalHarvester.hunt(container, 4, itemContainer, new Random(1))

    assert(itemContainer.resources === ArrayBuffer(ResourceGroup(Food,1), ResourceGroup(Feathers,1)))
    assert(container.size === 0)
  }

  test("hunt 1 rabbit") {

    val itemContainer = new ItemContainer(toolUtils)
    val container = new AnimalContainer(3)
    val a = Animal(anims.get(3), true)
    val b = Animal(anims.get(3), true)
    val c = Animal(anims.get(3), true)
    container.add(a)
    container.add(b)
    container.add(c)

    AnimalHarvester.hunt(container, 1, itemContainer, new Random(1))

    assert(itemContainer.resources === ArrayBuffer())
    assert(container.size === 2)
  }

  test("hunt 2 rabbits") {

    val itemContainer = new ItemContainer(toolUtils)
    val container = new AnimalContainer(3)
    val a = Animal(anims.get(3), true)
    val b = Animal(anims.get(3), true)
    val c = Animal(anims.get(3), true)
    container.add(a)
    container.add(b)
    container.add(c)

    AnimalHarvester.hunt(container, 2, itemContainer, new Random(1))

    assert(itemContainer.resources === ArrayBuffer(ResourceGroup(Food,1)))
    assert(container.size === 1)
  }
}
