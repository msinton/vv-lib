package com.consideredgames.game.model.animals

import com.consideredgames.game.model.resources.Resources.Food
import com.consideredgames.game.model.resources.{ItemContainer, ResourceGroup}

import scala.util.Random


/**
 * Created by matt on 09/03/15.
 */
object AnimalHarvester {

  /**
   * returns animals harvested
   */
  def harvest(container: AnimalContainer, howMany: Int, itemContainer: ItemContainer): List[Animal] = {

    var harvested: List[Animal] = List()

    if (validHarvestRequest(container, howMany)) {

        val animal = container.head.get
        val harvestInfo = animal.info.harvest
        val numRequired = harvestInfo.requires

        for (i <- 0 until (howMany / numRequired)) {

          itemContainer.add(ResourceGroup(Food, harvestInfo.meatGained))
          val res = harvestInfo.resourcesGained
          for (r <- res) {
            itemContainer.add(ResourceGroup(r.r, r.n))
        }
          for (j <- 0 until numRequired) {
            val animal = container.selectMostDispensable
            animal.exists{ a =>
              harvested = a :: harvested
              container.remove(a)
            }
          }
        }
    }

    harvested
  }

  /**
   * Only allow multiples of what is required
   * @return true if valid
   */
  def validHarvestRequest(container: AnimalContainer, howMany: Int): Boolean = {
    howMany match {
      case n if n > 0 && container.size >= n && (n % container.head.get.info.harvest.requires == 0) => true
      case _ => false
    }
  }

  def hunt(container: AnimalContainer, howMany: Int, itemContainer: ItemContainer, random: Random): List[Animal] = {

    var killed: List[Animal] = List()

    val animal = container.head.get
    val harvestInfo = animal.info.harvest
    val numRequired = harvestInfo.requires

    // check howMany against total in container and adjust
    val killNum = {
      if (howMany < container.size) {
        howMany
      } else {
        container.size
      }
    }

    val factor = killNum / numRequired
    val remainder = killNum % numRequired

      // get res from killing the remainder part
    {
      val meatNum = (harvestInfo.meatGained * remainder) / numRequired
      if (meatNum > 0)
        itemContainer.add(ResourceGroup(Food, meatNum))

      val res = harvestInfo.resourcesGained

      for (r <- res) {
        val num = (r.n * remainder) / numRequired
        if (num > 0)
          itemContainer.add(ResourceGroup(r.r, num))
      }
    }

      // get res from the factor part
    if (factor > 0) {
      val meatNum = harvestInfo.meatGained * factor
      itemContainer.add(ResourceGroup(Food, meatNum))

      val res = harvestInfo.resourcesGained

      for (r <- res) {
        val num = r.n * factor
        itemContainer.add(ResourceGroup(r.r, num))
      }
    }

    for (j <- 0 until killNum) {
      val animal = container.getRandomAnimal(random)
      animal.exists { a =>
        killed = a :: killed
        container.remove(a)
      }
    }
    killed
  }
}