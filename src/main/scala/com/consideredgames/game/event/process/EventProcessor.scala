package com.consideredgames.game.event.process

import com.consideredgames.game.event._
import com.consideredgames.game.model.animals.AnimalInfo
import com.consideredgames.game.model.game.{GameBuilder, NewGameConfig}
import com.consideredgames.game.model.person.tools.{ToolInfo, Tools}
import com.consideredgames.game.state.{Connectivity, StartGameError, State}
import com.consideredgames.message.Messages._

import scala.util.Try

/**
  * Created by matt on 18/07/17.
  */
object EventProcessor {

  def run(e: ConnectionEvent, state: State): State = {
    e match {
      case Connected => State(Connectivity(connected = true), state)
      case Disconnected => State(Connectivity(), state)
    }
  }

  private def readyGameToGameConfig(readyGame: NewGameReady): Try[NewGameConfig] = {
    val opts = readyGame.newGameOptions
    val animalInfosTry = opts.animalInfos.fold(AnimalInfo.importFromFile())(AnimalInfo.readFromString)
    val toolsTry: Try[Tools] = opts.tools.fold(Try(Tools())){json: String =>
      ToolInfo.readFromString(json) map {toolInfo => Tools(toolInfo)}}
    for {
      animalInfos <- animalInfosTry
      tools <- toolsTry
      config = NewGameConfig(readyGame.players, readyGame.seed, animalInfos, tools, List())
    } yield config
  }

  def run(e: GameActivityEvent, state: State): State = {
    val update = e match {
      case StartGame(id) =>
        val game = for {
          readyGame <- state.readyGames.find { _.gameId == id}
          config <- readyGameToGameConfig(readyGame).toOption
          profile <- state.profile
        } yield GameBuilder.build(config, profile.username)
        game.getOrElse(StartGameError("Game failed to start"))
    }

    State(update, state)
  }

  def run(event: Event, state: State): State = {
    event match {
      case m: Message => MessageProcessor.run(m, state)
      case c: ConnectionEvent => run(c, state)

      case g: GameActivityEvent => run(g, state)
    }
  }
}
