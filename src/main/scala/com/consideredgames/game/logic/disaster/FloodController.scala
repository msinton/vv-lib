package com.consideredgames.game.logic.disaster

import com.consideredgames.game.model.hex.{HexType, RiverNetwork, RiverSegment}

import scala.util.Random

class FloodController(riverNetwork: RiverNetwork, random: Random, roundsToFlood: Option[List[Int]]) {
  private var floodPercent: Int = 10
  private val roundsToFlood_ : List[Int] = roundsToFlood.getOrElse(List(2, 5, 8, 11, 13, 16, 18, 20, 21))

  def setFloodPercent(floodPercent: Int) {
    this.floodPercent = floodPercent
  }

  /**
   * Floods the land beside rivers if this is a flooding round.
   */
  def process(currentRound: Int) {
    if (roundsToFlood_.contains(currentRound)) {
      flood()
    }
  }

  private def flood() {

    def getHexOnSideOfRiverAtRandom(river: RiverSegment) = {
      river.hexB.map { hexB =>
        if (random.nextInt(2) == 1) {
          river.hexA
        }
        else {
          hexB
        }
      }.getOrElse(river.hexA)
    }

    for (river <- riverNetwork.getRivers) {
      if (random.nextInt(100) <= floodPercent) {
        val hex = getHexOnSideOfRiverAtRandom(river)

        if (hex.hexType != HexType.WATER) {
          hex.floodHex()
        }
      }
    }
  }
}