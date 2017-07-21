package com.consideredgames.game.state

/**
  * Created by matt on 15/07/17.
  */
case class Activity(joinedGames: Set[String] = Set(),
                    activeGameId: Option[String] = None) {

  def joinedGame(gameId: String): Activity = {
    val games = joinedGames + gameId
    Activity(joinedGames = games, activeGameId)
  }

  def activeGame(gameId: String): Activity = {
    Activity(joinedGames, Option(gameId))
  }
}

