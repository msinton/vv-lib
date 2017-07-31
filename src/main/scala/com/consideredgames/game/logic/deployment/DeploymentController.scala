package com.consideredgames.game.logic.deployment

import com.consideredgames.game.model.game.GameState
import com.consideredgames.game.model.person.NewPersonInstruction
import com.consideredgames.game.model.player.PlayerWithPeople
import com.consideredgames.message.{DeployBoat, DeployedBoat, DeployedPerson, Deployments}

/**
 * Created by matt on 08/10/15.
 */
case class DeploymentController(player: PlayerWithPeople, gameState: GameState, processor: DeploymentProcessor, players: Map[String, PlayerWithPeople]) {

  private val submitHandle: (List[DeployedPerson]) => Unit = submit()
  private val submitHandleWithBoat: (List[DeployedBoat], List[DeployedPerson]) => Unit = Function.uncurried(submit)

  //TODO - send message to client event processor
  private def submit(boats: List[DeployedBoat] = Nil)(deployedPersons: List[DeployedPerson]): Unit = {
    val message = Deployments(deployedPersons, boats)
    gameState.endTurn()
  }

  def deploy(deployedPeople: List[DeployedPerson], boats: List[DeployedBoat]): Unit = {

    players.get(gameState.turnState.currentPlayers.head).foreach { player =>
      processor.deploy(boats.map(_.boat))
      // on client we need to add the people to the player
      deployedPeople.foreach(person => player.create(person.person))
      processor.doDeploy(deployedPeople, player)
      gameState.endTurn()
    }
  }

  def getDeployerFor(peopleInstr: List[NewPersonInstruction], boats: List[DeployBoat] = Nil): Deploys = {
    val people = peopleInstr.map(player.create)

    if (gameState.turnState.round == 1) {
      val isFirstPlayer = gameState.turnState.nextPlayers.size == (gameState.turnState.data.playerOrder.size - 1)
      InitialDeploymentDeployer(player, people, processor, submitHandle, isFirstPlayer)
    } else {
      Deployer(player, people, boats, processor, submitHandleWithBoat)
    }
  }

}
