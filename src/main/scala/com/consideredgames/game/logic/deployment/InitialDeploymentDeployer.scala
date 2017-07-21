package com.consideredgames.game.logic.deployment

import com.consideredgames.game.model.hex.Hex
import com.consideredgames.game.model.person.Person
import com.consideredgames.game.model.player.PlayerWithPeople
import com.consideredgames.message.DeployedPerson

/**
 * Created by matt on 05/10/15.
 */
case class InitialDeploymentDeployer(player: PlayerWithPeople, peopleToDeploy: List[Person],
                                     deploymentProcessor: DeploymentProcessor,
                                     submitHandle: (List[DeployedPerson]) => Unit,
                                     firstPlayer: Boolean) extends Deploys {

  val boardUtils = deploymentProcessor.boardUtils

  val availableGroups = findAvailableGroups()

  var chosenGroup: collection.Set[Hex] = _

  def findAvailableGroups() = {
    if (firstPlayer)
      boardUtils.boardData.riverNetwork.getGroups
    else
      boardUtils.boardData.riverNetwork.getGroups.filter { hexGroup =>
        hexGroup.forall(_.person.isEmpty)
      }
  }

  def place(person: Person, hex: Hex) = {
    val placed = placePerson(person, hex)
    if (placed)
      chosenGroup = availableGroups.find(_.contains(hex)).getOrElse(throw new IllegalStateException("impossible deployment"))
    placed
  }

  def isValid(person: Person, hex: Hex) = {
    if (placedPeople.isEmpty) {
      availableGroups.exists(_.contains(hex))
    } else {
      isValidPosition(person, hex) && chosenGroup.contains(hex)
    }
  }

  def submit() = submitHandle(placedPeople)
}
