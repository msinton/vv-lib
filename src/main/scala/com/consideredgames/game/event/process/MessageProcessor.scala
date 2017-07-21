package com.consideredgames.game.event.process

import com.consideredgames.game.state.{Profile, State}
import com.consideredgames.message.Messages._

/**
  * Created by matt on 18/07/17.
  */
object MessageProcessor {

  def run(message: Message, state: State): State = {
    val update = message match {

      case RegisterResponseSuccess(username, _) => Profile(username)

      case JoinResponseSuccess(gameId) => state.activity.joinedGame(gameId)

      case NewGameResponse(gameId) => state.activity.joinedGame(gameId)

      case m: NewGameReady => m :: state.readyGames

      case x => println(s"unhandled message $x")

    }

    State(update, state)
  }
}
