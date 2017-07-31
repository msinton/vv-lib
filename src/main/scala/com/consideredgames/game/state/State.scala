package com.consideredgames.game.state

import com.consideredgames.game.model.game.GameData
import com.typesafe.scalalogging.LazyLogging

/**
  * Created by matt on 18/07/17.
  */
case class State(var connectivity: Connectivity = Connectivity(),
                 var activity: Activity = Activity(),
                 var profile: Option[Profile] = None,
                 var ready: ReadyGames = ReadyGames(Nil),
                 var game: Option[GameData] = None,
                 var errors: List[Error] = Nil
                )

object State extends LazyLogging {

  def apply(update: Any, state: State): State = {

    update match {
      case u: Connectivity => state.connectivity = u
      case u: Profile => state.profile = Option(u)
      case u: Activity => state.activity = u
      case u: ReadyGames => state.ready = u
      case u: Errors => state.errors = u.errors
      case u: GameData => state.game = Option(u)
      case _: Unit => // do nothing
      case x => logger.error(s"unexpected state update: $x")
    }

    state
  }

  def apply(stateUpdates: Seq[Any], state: State): State = {
    stateUpdates.foldLeft(state)((state: State, update: Any) => apply(update, state))
  }
}