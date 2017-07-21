package com.consideredgames.game.model.animals

import com.consideredgames.game.model.hex.{Hex, HexType}
import com.consideredgames.game.model.person.Person

import scala.util.Random

/**
 * Created by matt on 09/03/15.
 */
class AnimalManager(animalInfos: List[AnimalInfo], hexOrPerson: Either[Hex, Person]) {

  import AnimalManager._

  val containers: collection.mutable.Map[AnimalInfo, AnimalContainer] = {
    val m = collection.mutable.AnyRefMap.empty[AnimalInfo, AnimalContainer]
    for (animalInfo <- animalInfos) {
      if (hexOrPerson.isLeft) {
        m.put(animalInfo, new AnimalContainer(animalInfo.hexCapacity))
      } else {
        m.put(animalInfo, new AnimalContainer(animalInfo.personCapacity))
      }
    }
    m
  }

  def add(animal: Animal): Boolean = containers(animal.info).add(animal)

  def levelUpCapacity() {
    for (container <- containers) {
      if (hexOrPerson.isLeft) {
        container._2.capacity += container._1.hexCapacity / 6
      } else {
        container._2.capacity += container._1.personCapacity / 6
      }
    }
  }

  def resetToBaseCapacity() {
    for (container <- containers) {
      if (hexOrPerson.isLeft) {
        container._2.capacity = container._1.hexCapacity
      } else {
        container._2.capacity = container._1.personCapacity
      }
    }
  }

  def allocateAnimal(a: Animal, random: Random): Boolean = {
    val animalInfo = a.info
    val container = containers(animalInfo)

    def allocateForHex(hex: Hex): Boolean = {
      // add if room
      if (!container.isFull) add(a)
      else { // animal attempts to move to a random neighbour hex, dies if that hex is already full.
      //TODO only traversable by foot neighbours
      var neighbours = hex.neighbours.values.toIndexedSeq
        var i = 0

        if (neighbours.nonEmpty) {

          i = random.nextInt(neighbours.size)
          val tempHex = neighbours(i)
          neighbours = neighbours.filter { h => h == tempHex}

          // if neighbour is full then the animal dies (disappears/was never born)!
          addAnimalTo(tempHex, a, animalInfos)
        }
        else
          false
      }
    }

    // this is a manager for a person
    def allocateForPerson(person: Person): Boolean = {
      // if animals breed while being carried, they fill the carried space, or try to live in the hex they are on
      if (container.isFull) {

        // only hex type water applies and if not on a hex then give up!
        return (person.hex filter (_.hexType != HexType.WATER)).exists(createAnimalManagerIfNeeded(_, animalInfos).allocateAnimal(a, random))
      }
      container.add(a)
    }

    hexOrPerson.fold( allocateForHex, allocateForPerson)
  }

  /**
   * @return the number of animals killed
   */
  def killAnimalsThatExceedCapacity(random: Random) = {

    var killed = 0

    for (container <- containers) {
      if (container._2.capacity < container._2.size) {

        // kill at least one
        container._2.randomRemove(random) foreach (_ => killed += 1)
        // kill more if overlap is high
        val overlap = container._2.size - container._2.capacity
        if (hexOrPerson.isLeft) {
          // TWEAK the killing factor: depending on the proportion of the excess, kill more or less
          // currently if overlap is twice as big as capacity, kill 1 extra
          if (overlap * 2 > container._1.hexCapacity)
            container._2.randomRemove(random) foreach (_ => killed += 1)
        }
      }
    }
    killed
  }

  /**
   * Impregnates then progresses pregnancy, so animals with term 1 or less give birth immediately.
   */
  def progressPregnancies(random: Random) = {
    PregnancyManager.tryImpregnateAll(this, random)
    PregnancyManager.progressPregnancy(this, random)
  }
}


object AnimalManager {

  def addAnimalTo(hex: Hex, animal: Animal, animalInfos: List[AnimalInfo]) = createAnimalManagerIfNeeded(hex, animalInfos).add(animal)

  def createAnimalManagerIfNeeded(hex: Hex, animalInfos: List[AnimalInfo]): AnimalManager = {
    hex.animalManager getOrElse {
      val animalMan = new AnimalManager(animalInfos, Left(hex))
      hex.animalManager = Option(animalMan)
      animalMan
    }
  }

  def addAnimalTo(person: Person, animal: Animal, animalInfos: List[AnimalInfo]) = createAnimalManagerIfNeeded(person, animalInfos).add(animal)

  def createAnimalManagerIfNeeded(person: Person,  animalInfos: List[AnimalInfo]) = {
    person.animalManager getOrElse {
      val animalMan = new AnimalManager(animalInfos, Right(person))
      person.animalManager = Option(animalMan)
      animalMan
    }
  }
}