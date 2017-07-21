package com.consideredgames.game.model.animals

import com.consideredgames.game.model.hex.{Hex, HexType, Side}
import com.consideredgames.game.model.person.Person
import com.consideredgames.game.model.player.PlayerColours._
import org.scalatest.{FunSuite, OptionValues}

import scala.util.Random

/**
 * Created by matt on 09/03/15.
 */
class AnimalManagerTest extends FunSuite with OptionValues {

  val animalInfos = AnimalInfo.importFromFile().get

  test("level up capacity") {

    val h = Hex(1, HexType.CLAY)

    val manager = new AnimalManager(animalInfos, Left(h))

    manager.levelUpCapacity()

    assert(manager.containers(animalInfos.find { a => a.name == "horse"}.get).capacity === 9) //8     1
    assert(manager.containers(animalInfos.find { a => a.name == "boar"}.get).capacity === 14) //12    2
    assert(manager.containers(animalInfos.find { a => a.name == "chicken"}.get).capacity === 28) //24 4
    assert(manager.containers(animalInfos.find { a => a.name == "rabbit"}.get).capacity === 42) //36  6

    manager.levelUpCapacity()

    assert(manager.containers(animalInfos.find { a => a.name == "horse"}.get).capacity === 10) //     1
    assert(manager.containers(animalInfos.find { a => a.name == "boar"}.get).capacity === 16) //      2
    assert(manager.containers(animalInfos.find { a => a.name == "chicken"}.get).capacity === 32) //   4
    assert(manager.containers(animalInfos.find { a => a.name == "rabbit"}.get).capacity === 48) //    6

    manager.levelUpCapacity()

    assert(manager.containers(animalInfos.find { a => a.name == "horse"}.get).capacity === 11) //     1
    assert(manager.containers(animalInfos.find { a => a.name == "boar"}.get).capacity === 18) //      2
    assert(manager.containers(animalInfos.find { a => a.name == "chicken"}.get).capacity === 36) //   4
    assert(manager.containers(animalInfos.find { a => a.name == "rabbit"}.get).capacity === 54) //    6

    manager.levelUpCapacity()

    assert(manager.containers(animalInfos.find { a => a.name == "horse"}.get).capacity === 12) //     1
    assert(manager.containers(animalInfos.find { a => a.name == "boar"}.get).capacity === 20) //      2
    assert(manager.containers(animalInfos.find { a => a.name == "chicken"}.get).capacity === 40) //   4
    assert(manager.containers(animalInfos.find { a => a.name == "rabbit"}.get).capacity === 60) //    6

    manager.resetToBaseCapacity()

    assert(manager.containers(animalInfos.find { a => a.name == "horse"}.get).capacity === 8)
    assert(manager.containers(animalInfos.find { a => a.name == "boar"}.get).capacity === 12)
    assert(manager.containers(animalInfos.find { a => a.name == "chicken"}.get).capacity === 24)
    assert(manager.containers(animalInfos.find { a => a.name == "rabbit"}.get).capacity === 36)
  }

  test("that progress pregnancies is as expected for horses") {

    val h = Hex(1, HexType.CLAY)

    val manager = AnimalManager.createAnimalManagerIfNeeded(h, animalInfos)
    manager.levelUpCapacity()
    manager.levelUpCapacity()
    manager.levelUpCapacity()
    manager.levelUpCapacity()

    val info = animalInfos.find(_.name == "horse").value

    for (i <- 1 to 6) {
      assert(manager.add(Animal(info, female = !{i % 5 == 0}, wild = false)))
    }

    val r = new Random(3)

    manager.progressPregnancies(r)

    var preggers = h.animalManager.value.containers.get(info).value.filterByPregnant(pregnant = true)

    assert(preggers.size === 4)

    for (a <- preggers) {
      assert(a.pregnant.value.term === 2)
    }

    // next round
    manager.progressPregnancies(r)
    preggers = h.animalManager.value.containers.get(info).value.filterByPregnant(pregnant = true)
    assert(preggers.size === 5)

    assert(preggers.count {_.pregnant.value.term == 1} === 4)
    assert(preggers.count {_.pregnant.value.term == 2} === 1)

    assert(h.animalManager.value.containers.get(info).value.size === 6)
    // and again
    manager.progressPregnancies(r)
    preggers = h.animalManager.value.containers.get(info).value.filterByPregnant(pregnant = true)
    assert(preggers.size === 5)

    assert(preggers.count {_.pregnant.value.term == 0} === 4)
    assert(preggers.count {_.pregnant.value.term == 1} === 1)

    assert(h.animalManager.value.containers.get(info).value.size === 6)
    // and again - they give birth now
    manager.progressPregnancies(r)
    preggers = h.animalManager.value.containers.get(info).value.filterByPregnant(pregnant = true)
    assert(preggers.size == 1)
    assert(h.animalManager.value.containers.get(info).value.size == 10)
  }

  test("that progress pregnancies is as expected for boars") {

    val h = Hex(1, HexType.CLAY)
    val neighbour = Hex(2, HexType.PLAINS)
    h.neighbours.put(Side.north, neighbour)

    val manager = AnimalManager.createAnimalManagerIfNeeded(h, animalInfos)
    manager.levelUpCapacity()
    manager.levelUpCapacity()
    manager.levelUpCapacity()
    manager.levelUpCapacity()

    val info = animalInfos.find(_.name == "boar").value

    for (i <- 1 to 18) {
      assert(manager.add(Animal(info, female = {i % 5 == 0}, wild = false)))
    }

    val r = new Random(3)

    manager.progressPregnancies(r)

    var preggers = h.animalManager.value.containers.get(info).value.filterByPregnant(pregnant = true)

    assert(preggers.size === 3)

    for (a <- preggers)
      assert(a.pregnant.value.term === 1)

    // next round
    manager.progressPregnancies(r)
    preggers = h.animalManager.value.containers.get(info).value.filterByPregnant(pregnant = true)
    assert(preggers.size === 3)
    for (a <- preggers)
      assert(a.pregnant.value.term === 0)

    assert(h.animalManager.value.containers.get(info).value.size === 18)
    // and again
    manager.progressPregnancies(r)
    preggers = h.animalManager.value.containers.get(info).value.filterByPregnant(pregnant = true)
    assert(preggers.isEmpty)
    for (a <- preggers)
      assert(a.pregnant.value.term === 1)

    assert(h.animalManager.value.containers.get(info).value.size === 20)

    // One must have been allocated to hex neighbour
    assert(neighbour.animalManager.value.containers.get(info).value.size === 1)
  }

  test("that killAnimalsThatExceedCapacity ok") {

    val h = Hex(1, HexType.CLAY)
    val neighbour = Hex(2, HexType.PLAINS)
    h.neighbours.put(Side.north, neighbour)

    val manager = AnimalManager.createAnimalManagerIfNeeded(h, animalInfos)
    manager.levelUpCapacity()
    manager.levelUpCapacity()
    manager.levelUpCapacity()
    manager.levelUpCapacity()

    val info = animalInfos.find(_.name == "boar").value

    for (i <- 1 to 20)
      assert(manager.add(Animal(info, female = {i % 5 == 0}, wild = false)))

    val r = new Random(1)

    assert(manager.killAnimalsThatExceedCapacity(r) === 0)

    manager.resetToBaseCapacity()

    // just so happens to be 2 at first (with randomness) - this is good though
    assert(manager.killAnimalsThatExceedCapacity(r) === 2)

    while (manager.containers.get(info).value.size > 12) {
      assert(manager.killAnimalsThatExceedCapacity(r) >= 1)
    }
  }

  test("that progress pregnancies is as expected for rabbits in person - overflow goes into hex") {

    val p = Person(1, Black)
    val h = Hex(1, HexType.CLAY)
    p.hex = Option(h)

    val manager = AnimalManager.createAnimalManagerIfNeeded(p, animalInfos)
    manager.levelUpCapacity()
    manager.levelUpCapacity()
    manager.levelUpCapacity()
    manager.levelUpCapacity()

    val info = animalInfos.find(_.name == "rabbit").value

    assert(manager.containers.get(info).value.capacity === 10)

    for (i <- 1 to 9)
      assert(manager.add(Animal(info, female = !{i % 8 == 0}, wild = false)))

    val r = new Random(1)
    // rabbits immediately have babies, so they will never appear to be pregnant
    manager.progressPregnancies(r)
    val preggers = p.animalManager.value.containers.get(info).value.filterByPregnant(pregnant = true)
    assert(preggers.size === 0)

    assert(manager.containers.get(info).value.size === 10)

    assert(h.animalManager.value.containers.get(info).value.size === 9)
  }

  test("that progress pregnancies is as expected for rabbits in person - goes nowhere if on water") {

    val p = Person(1, Black)
    val h = Hex(1, HexType.WATER)
    p.hex = Option(h)

    val manager = AnimalManager.createAnimalManagerIfNeeded(p, animalInfos)
    manager.levelUpCapacity()
    manager.levelUpCapacity()
    manager.levelUpCapacity()
    manager.levelUpCapacity()

    val info = animalInfos.find(_.name == "rabbit").value

    assert(manager.containers.get(info).value.capacity === 10)

    for (i <- 1 to 9)
      assert(manager.add(Animal(info, female = !{i % 8 == 0}, wild = false)))

    val r = new Random(1)
    // rabbits immediately have babies, so they will never appear to be pregnant
    manager.progressPregnancies(r)
    val preggers = p.animalManager.value.containers.get(info).value.filterByPregnant(pregnant = true)
    assert(preggers.size === 0)

    assert(manager.containers.get(info).value.size === 10)

    assert(h.animalManager === None)
  }

  test("that animals are not born if hex is full") {

    val p = Person(1, Black)
    val h = Hex(1, HexType.WATER)
    p.hex = Option(h)

    val manager = AnimalManager.createAnimalManagerIfNeeded(p, animalInfos)
    val hManager = AnimalManager.createAnimalManagerIfNeeded(h, animalInfos)

    val info = animalInfos.find(_.name == "rabbit").value

    assert(manager.containers.get(info).value.capacity === 6)

    for (_ <- 1 to 6)
      assert(manager.add(Animal(info, female = true)))

    for (_ <- 1 to 36)
      assert(hManager.add(Animal(info, female = true)))

    val r = new Random(1)

    for (_ <- 1 to 10) {
      assert(!manager.allocateAnimal(Animal(info, female = true), r))
      assert(!hManager.allocateAnimal(Animal(info, female = true), r))
    }

    assert(manager.containers.get(info).value.size === 6)

    assert(h.animalManager.value.containers.get(info).value.size === 36)
  }

}
