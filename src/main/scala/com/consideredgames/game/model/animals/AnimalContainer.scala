package com.consideredgames.game.model.animals

import scala.util.Random

/**
 * Created by matt on 09/03/15.
 */
class AnimalContainer(var capacity: Int) {

  private var animals = new collection.mutable.ArrayBuffer[Animal](capacity)

  def containedType: Option[AnimalInfo] = {
    if (animals.isEmpty)
      return None
    Option(animals(0).info)
  }

  def head = {
    animals.isEmpty match {
      case true => None
      case false => Option(animals(0))
    }
  }

  def size = animals.size

  def isFull = size >= capacity

  def add(animal: Animal): Boolean = {
    if (!isFull) {
      if (animals.isEmpty || containedType.get == animal.info) {
        animals += animal
        return true
      }
    }
    false
  }

  def remove(animal: Animal): Boolean = {
    val newAnimals = animals - animal
    val removed = newAnimals.size < animals.size
    animals = newAnimals
    removed
  }

  def getRandomAnimal(random: Random): Option[Animal] = {
    animals.isEmpty match {
      case false =>
        val index = random.nextInt(animals.size)
        Option(animals(index))
      case _ => None
    }
  }

  def randomRemove(random: Random): Option[Animal] = {
    getRandomAnimal(random) map {a =>
      remove(a)
      a
    }
  }

  def filterByFemale(female: Boolean): collection.Seq[Animal] = {
    animals.filter { a => a.female == female}
  }

  def filterByPregnant(pregnant: Boolean): collection.Seq[Animal] = {
    animals.filter { a => a.isPregnant == pregnant}
  }

  def notPregnantFemales = {
    animals.filter { a => !a.isPregnant && a.female}
  }

  def tame = {
    animals.filter(!_.wild)
  }

  /**
   * @return male->if-more-than-one else female->if-not-pregnant else females-pregnant->if-more-than-one else any male else remaining 1 pregnant
   */
  def selectMostDispensable: Option[Animal] = {

    if (animals.isEmpty) return None

    val males = filterByFemale(female = false)
    males.size match {

      case n if n > 1 => Option(males(0))
      case _ =>
        val femalesNotPregnant = filterByFemale(female = true).filter { a => !a.isPregnant}

        femalesNotPregnant.size match {
          case m if m > 0 => Option(femalesNotPregnant(0))
          case _ =>
            val pregnant = filterByPregnant(pregnant = true)

            pregnant.size match {
              case p if p > 1 => Option(pregnant(0))
              case _ =>

                males.size match {
                  case mm if mm > 0 => Option(males(0))
                  //Just 1 pregnant female left
                  case _ => head
                }
            }
        }
    }
  }
}