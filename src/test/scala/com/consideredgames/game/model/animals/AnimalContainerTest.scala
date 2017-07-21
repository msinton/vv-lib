package com.consideredgames.game.model.animals

import org.scalatest.FunSuite

import scala.util.Random

/**
 * Created by matt on 09/03/15.
 */
class AnimalContainerTest extends FunSuite {

  val animalInfos = AnimalInfo.importFromFile()

  test("that remove works") {

    val container = new AnimalContainer(1)

    val a = Animal(animalInfos.get(0), true)

    container.add(a)

    assert(container.remove(a) === true)
    assert(container.size === 0)
  }

  test("that remove does not remove if not there") {

    val container = new AnimalContainer(1)

    val a = Animal(animalInfos.get(0), female = true)
    val b = Animal(animalInfos.get(1), female = true)

    container.add(a)

    assert(container.remove(b) === false)
    assert(container.size === 1)
  }

  test("that cant add more than capacity") {

    val container = new AnimalContainer(1)

    val a = Animal(animalInfos.get(0), true)
    val b = Animal(animalInfos.get(0), true)

    container.add(a)

    assert(container.add(b) === false)
    assert(container.size === 1)
  }

  test("that selectMostDispensable selects not pregnant female first") {
    val container = new AnimalContainer(3)

    val a = Animal(animalInfos.get(0), true)
    val b = Animal(animalInfos.get(0), true, Pregnant(1))
    val male = Animal(animalInfos.get(0), false)

    container.add(a)
    container.add(b)
    container.add(male)

    val selected = container.selectMostDispensable.get
    assert(selected === a)
  }

  test("that selectMostDispensable selects male first when more than one") {
    val container = new AnimalContainer(4)

    val a = Animal(animalInfos.get(0), true)
    val b = Animal(animalInfos.get(0), true, Pregnant(1))
    val male = Animal(animalInfos.get(0), false)

    container.add(a)
    container.add(b)
    container.add(male)
    container.add(male)

    val selected = container.selectMostDispensable.get
    assert(selected === male)
  }

  test("that selectMostDispensable selects male first when only 1 pregnant female") {
    val container = new AnimalContainer(4)

    val a = Animal(animalInfos.get(0), true, Pregnant(1))
    val male = Animal(animalInfos.get(0), false)

    container.add(a)
    container.add(male)

    val selected = container.selectMostDispensable.get
    assert(selected === male)
  }

  test("that selectMostDispensable selects pregnant first when > 1 pregnant and none are not pregnant, only 1 male") {
    val container = new AnimalContainer(4)

    val a = Animal(animalInfos.get(0), true, Pregnant(1))
    val b = Animal(animalInfos.get(0), true, Pregnant(1))
    val male = Animal(animalInfos.get(0), false)

    container.add(a)
    container.add(b)
    container.add(male)

    val selected = container.selectMostDispensable.get
    assert(selected == a || selected == b)
  }

  test("that selectMostDispensable selects pregnant, when all there is") {
    val container = new AnimalContainer(4)

    val a = Animal(animalInfos.get(0), true, Pregnant(1))

    container.add(a)

    val selected = container.selectMostDispensable.get
    assert(selected == a)
  }

  test("that selectMostDispensable just one male") {
    val container = new AnimalContainer(4)

    val a = Animal(animalInfos.get(0), false)

    container.add(a)

    val selected = container.selectMostDispensable.get
    assert(selected == a)
  }

  test("that random remove works") {

    val container = new AnimalContainer(4)

    val a = Animal(animalInfos.get(0), female = true, Pregnant(1))
    val b = Animal(animalInfos.get(0), female = true, Pregnant(2))
    val c = Animal(animalInfos.get(0), female = true, Pregnant(3))
    val d = Animal(animalInfos.get(0), female = true, Pregnant(4))

    container.add(a)
    container.add(b)
    container.add(c)
    container.add(d)

    var r = new Random(1)

    assert(container.randomRemove(r).get.pregnant.get.term === 3)
    assert(container.randomRemove(r).get.pregnant.get.term === 2)
    assert(container.randomRemove(r).get.pregnant.get.term === 1)
    assert(container.randomRemove(r).get.pregnant.get.term === 4)

    container.add(a)
    container.add(b)
    container.add(c)
    container.add(d)
    r = new Random(2)

    assert(container.randomRemove(r).get.pregnant.get.term === 3)
    assert(container.randomRemove(r).get.pregnant.get.term === 1)
    assert(container.randomRemove(r).get.pregnant.get.term === 4)
    assert(container.randomRemove(r).get.pregnant.get.term === 2)
  }

  test("that isFull works") {

    val container = new AnimalContainer(1)
    val a = Animal(animalInfos.get(0), true)
    container.add(a)

    assert(container.isFull)
  }

  test("that can't add different types") {
    val container = new AnimalContainer(2)

    val a = Animal(animalInfos.get(0), true)
    val b = Animal(animalInfos.get(1), true)

    container.add(a)
    assert(!container.add(b))

    assert(container.size === 1)
    assert(container.containedType.get === animalInfos.get(0))
  }

  test("head") {

    val c = new AnimalContainer(2)

    assert(c.head === None)

    c.add(Animal(animalInfos.get(0), true))

    assert(c.head !== None)
  }

}