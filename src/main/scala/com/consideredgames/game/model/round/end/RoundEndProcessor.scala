package com.consideredgames.game.model.round.end

import com.consideredgames.game.model.animals.AnimalManager
import com.consideredgames.game.model.board.BoardData

import scala.util.Random

/**
 * Created by matt on 16/03/15.
 */
class RoundEndProcessor(boardData: BoardData, random: Random) {

  def process() = {

    for (animalManagers: AnimalManager <- boardData.animalManagers) {
      animalManagers.progressPregnancies(random)
      animalManagers.killAnimalsThatExceedCapacity(random) // TWEAK: the order of these could be swapped! Killing first will result in fewer animals.
    }
  }
}
