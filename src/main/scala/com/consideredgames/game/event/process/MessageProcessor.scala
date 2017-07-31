package com.consideredgames.game.event.process

import com.consideredgames.game.state._
import com.consideredgames.message.Messages._

/**
  * Created by matt on 18/07/17.
  */
object MessageProcessor {

  def run(message: Message, state: State): State = {
    val updates: Seq[Any] = message match {

      case RegisterResponseSuccess(username, _) => Seq(
        Profile(username),
        Errors(Nil))

      case LoginResponseSuccess(username, _) => Seq(
        Profile(username),
        Errors(Nil))

      case JoinResponseSuccess(gameId) => Seq(state.activity.joinedGame(gameId))
      case NewGameResponse(gameId) => Seq(state.activity.joinedGame(gameId))
      case m: NewGameReady => Seq(ReadyGames(m :: state.ready.games))

      case m: Error => Seq(Errors(m, state.errors))

      case SessionStarted() => Nil // do nothing
      case x => println(s"unhandled message $x")
        Nil
    }

    State(updates, state)
  }
}
