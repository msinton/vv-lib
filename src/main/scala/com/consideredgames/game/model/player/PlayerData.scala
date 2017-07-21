package com.consideredgames.game.model.player

import scala.collection._

trait HasPlayers {
  def players: mutable.LinkedHashMap[Int, PlayerWithPeople]
}

/**
 * PlayerData contains each player.
 *
 * @author Matthew
 *
 */
case class PlayerData(players: mutable.LinkedHashMap[Int, PlayerWithPeople]) extends HasPlayers {
  /** players key: number of joining game */

  def numberOfPlayers: Int = players.size

  /**
   * Adds the player to internal list. The order added is maintained.
   */
  def add(player: PlayerWithPeople) {
    if (players.isEmpty) players.put(0, player) else players.put(players.last._1 + 1, player)
  }

  def getPlayer(number: Int) = players.get(number)

  def getPlayerNumbers = players.keySet
}

object PlayerData {
  def apply(playerData: HasPlayers): PlayerData = {
    val playersWithNumbersReset = playerData.players.zipWithIndex.map { o => (o._2, o._1._2) }
    PlayerData(playersWithNumbersReset)
  }
}
