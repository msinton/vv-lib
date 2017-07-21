package com.consideredgames.game.logic.deployment

import com.consideredgames.game.model.deployment.HexLocation
import com.consideredgames.game.model.hex.{Hex, Side}
import com.consideredgames.game.model.person.Person
import com.consideredgames.game.model.player.PlayerWithPeople
import com.consideredgames.message.{DeployBoat, DeployedBoat, DeployedPerson}

/**
 * Created by matt on 05/10/15.
 */
case class Deployer(player: PlayerWithPeople, peopleToDeploy: List[Person], boats: List[DeployBoat],
                    deploymentProcessor: DeploymentProcessor, submitHandle: (List[DeployedBoat], List[DeployedPerson]) => Unit) extends Deploys {

  var placedBoats = List[DeployedBoat]()

  def isValid(person: Person, hex: Hex) = isValidPosition(person, hex)

  def place(person: Person, hex: Hex) = placePerson(person, hex)

  def placeBoat(hex: Hex, side: Side) = {
    val boat = deploymentProcessor.deployBoat(hex, side)
    placedBoats = DeployedBoat(boat) :: placedBoats
  }

  def submit() = submitHandle(placedBoats, placedPeople)
}

trait Deploys {
  val peopleToDeploy: List[Person]
  val deploymentProcessor: DeploymentProcessor
  var placedPeople = List[DeployedPerson]()

  def isValid(person: Person, hex: Hex): Boolean
  def place(person: Person, hex: Hex): Boolean

  def toPlace = peopleToDeploy.toSet -- placedPeople.map(_.person).toSet


  def undo(hex: Hex) = {
    placedPeople = placedPeople.filterNot { case deployed =>
      deployed.location match {
        case HexLocation(h) => h == hex
        case _ => false
      }
    }
  }

  def finished() = peopleToDeploy.size == placedPeople.size

  def submit(): Unit

  protected def placePerson(person: Person, hex: Hex) = {
    if (isValid(person, hex)) {
      deploymentProcessor.deploy(person, hex)
      placedPeople = DeployedPerson(person, HexLocation(hex)) :: placedPeople
      true
    } else {
      false
    }
  }

  protected def isValidPosition(person: Person, hex: Hex): Boolean = deploymentProcessor.valid(person, hex)
}
