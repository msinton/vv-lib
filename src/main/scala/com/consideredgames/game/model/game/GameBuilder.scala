package com.consideredgames.game.model.game

import com.consideredgames.game.logic.TurnOrderGenerator
import com.consideredgames.game.logic.deployment.{DeploymentController, DeploymentProcessor}
import com.consideredgames.game.logic.principal.{PrincipalActionsController, PrincipalActionsProcessor, PrincipalEndController}
import com.consideredgames.game.logic.season.WeatherManager
import com.consideredgames.game.model.board.{BoardData, BoardUtils}
import com.consideredgames.game.model.person.tools.{Tool, ToolUtils}
import com.consideredgames.game.model.player._
import com.consideredgames.game.model.resources.ItemContainer
import com.consideredgames.game.model.round.principal.Actions
import com.consideredgames.game.model.season.WeatherWeightings

import scala.util.Random

/**
 * Created by matt on 03/09/15.
 */
object GameBuilder {

  private def setupPlayers(players: List[Player], myName: String, toolUtils: ToolUtils): (List[PlayerWithPeople], FullPlayer) = {
    val (me, others) = players
      .partition(_.name == myName)

    val otherPlayers = others.map { p => OtherPlayer(p.name, p.colour)}

    val myPlayer = FullPlayer(myName, ItemContainer(toolUtils), me.head.colour)

    (myPlayer :: otherPlayers, myPlayer)
  }

  def build(newGameConfig: NewGameConfig, myName: String): GameData = {

    val toolUtils = ToolUtils(newGameConfig.tools)

    val (players, player) = setupPlayers(newGameConfig.players, myName, toolUtils)

    val random = new Random(newGameConfig.seed)
    val boardData = new BoardData(newGameConfig.players.size, random, newGameConfig.animalInfos)
    val boardUtils = BoardUtils(boardData)
    val weatherManager = new WeatherManager(WeatherWeightings.defaultEasy, random)
    val deploymentProcessor = DeploymentProcessor(boardUtils)
    val principalActionsProcessor = new PrincipalActionsProcessor(new Actions(toolUtils, newGameConfig.animalInfos), random, weatherManager, boardData)

    val gameProcessors = GameProcessors(deploymentProcessor, principalActionsProcessor)

    val turnOrder = TurnOrderGenerator.generate(players, random)
    val turnState = TurnState(TurnData(turnOrder, Phases.phases, 15))

    val gameState = GameState(turnState)
    val playersAsMap = players.map{p => (p.name, p)}.toMap
    val deploymentController = DeploymentController(player, gameState, deploymentProcessor, playersAsMap)
    val principalActionsController = PrincipalActionsController(player, gameState, playersAsMap, principalActionsProcessor)
    val principalEndController = PrincipalEndController()
    val gameControllers = GameControllers(deploymentController, principalActionsController, principalEndController)

    // add starting tools
    newGameConfig.startingTools.foreach{ order =>
      toolUtils.allToolInfo.toolsByName.get(order.name).foreach { tool =>
        (0 until order.n).foreach(_ => player.itemContainer.add(tool, Tool(tool.startLife)))
      }
    }

    GameData(boardUtils, playersAsMap, toolUtils, gameProcessors, gameState, gameControllers)
  }

}
