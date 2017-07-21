package com.consideredgames.game.state

import com.consideredgames.game.model.game.GameData
import com.consideredgames.message.Messages.NewGameReady
import com.typesafe.scalalogging.LazyLogging

/**
  * Created by matt on 18/07/17.
  */
case class State(connectivity: Connectivity = Connectivity(),
                 activity: Activity = Activity(),
                 profile: Option[Profile] = None,
                 readyGames: List[NewGameReady] = Nil,
                 game: Option[GameData] = None,
                 errors: List[Errors] = Nil
                )

object State extends LazyLogging {

  def apply(update: Any, state: State): State = {

    var connectivity = state.connectivity
    var activity = state.activity
    var profile = state.profile
    var readyGames = state.readyGames
    var errors = state.errors
    var game = state.game

    update match {
      case u: Connectivity => connectivity = u
      case u: Profile => profile = Option(u)
      case u: Activity => activity = u
      case u: List[NewGameReady] => readyGames = u
      case u: List[Errors] => errors = u
      case u: GameData => game = Option(u)
      case x => logger.error(s"unexpected state update: $x")
    }

    State(connectivity, activity, profile, readyGames)
  }
}