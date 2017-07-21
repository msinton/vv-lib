package com.consideredgames.game.model.animals

import scala.util.Random
import scalaz.Scalaz._

/**
 * Created by matt on 17/03/15.
 */
object PregnancyManager {

  val wildPregnancyChance = 5
  val tamePregnancyChance = 7
  val pregnancyChanceDenominator = 10

  def tryImpregnateAll(animalManager: AnimalManager, random: Random): Unit = {

    for (kv <- animalManager.containers) {
      if (kv._2.filterByFemale(false).nonEmpty) {
        for (candidate <- kv._2.notPregnantFemales) {

          val pregnancyChance = if (candidate.wild) wildPregnancyChance else tamePregnancyChance
          val doImpregnate = pregnancyChance > random.nextInt(pregnancyChanceDenominator)
          // make pregnant if doImpregnate is true
          candidate.pregnant = doImpregnate option Pregnant(kv._1.pregnancy.duration)
        }
      }
    }
  }

  /**
   * Pregnancy term progressed. If term is less than 0, give birth. AnimalManager allocates the newborn.
   */
  def progressPregnancy(animalManager: AnimalManager, random: Random) = {
    for (kv <- animalManager.containers) {
      for (animal <- kv._2.filterByPregnant(pregnant = true)) {

        animal.pregnant.foreach({
          preg => preg.term -= 1
            if (preg.term < 0) {
              val litter = random.nextInt(animal.info.pregnancy.maxLitterSize) + 1
              for (i <- 1 to litter) {
                animalManager.allocateAnimal(Animal(kv._1, random.nextBoolean()), random)
                animal.pregnant = None
              }
            }
        })
      }
    }
  }

}
