package com.consideredgames.game.state

/**
  * Created by matt on 15/07/17.
  */
case class Activity(joinedGames: List[String] = Nil,
                    activeGameId: Option[String] = None) {

  def joinedGame(gameId: String): Activity = {
    val games = gameId :: joinedGames
    Activity(joinedGames = games, activeGameId)
  }

  def activeGame(gameId: String): Activity = {
    Activity(joinedGames, Option(gameId))
  }
}

