package com.consideredgames.game.logic

import com.consideredgames.game.model.player.Player

import scala.util.Random

/**
 * Created by matt on 04/04/15.
 */
object TurnOrderGenerator {

  def generate(players: List[Player], random: Random) = {
    random.shuffle(players.map(_.name))
  }
}
