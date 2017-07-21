package com.consideredgames.message


import com.consideredgames.game.model.animals.Animal
import com.consideredgames.game.model.deployment.Location
import com.consideredgames.game.model.hex.Boat
import com.consideredgames.game.model.person.{NewPersonInstruction, Person}
import com.consideredgames.game.model.person.tools.{RichToolInfo, Tool}
import com.consideredgames.game.model.round.principal.{ActionFulfillment, ActionResult, AttackResults}

/**
 * Created by matt on 27/02/15.
 */
abstract class GameMessage

trait Movement
case class PersonMovement(person: Person, location: Location) extends Movement
case class BoatMovement(boatFrom: Boat, boatTo: Boat) extends Movement
case class Movements(movements: Seq[Movement]) extends GameMessage

case class PersonToActionResult(person: Person, actionResult: ActionResult)

case class PrincipalAction(actions: Seq[ActionFulfillment], assignedTools: collection.Map[(RichToolInfo, Int), Tool]) extends GameMessage

// deployment phase message
case class DeployedPerson(person: Person, location: Location)
case class DeployedBoat(boat: Boat)

case class DeployBoat()

/** units already placed by another */
case class Deployments(deployedPeople: List[DeployedPerson], boats: List[DeployedBoat] = Nil) extends GameMessage
/** units to place */
case class ToDeploys(people: List[NewPersonInstruction], boats: List[DeployBoat]) extends GameMessage

case class PrincipalPhasePreProcess(attackResults: AttackResults,
                                    allActionResults: List[PersonToActionResult],
                                    killed: List[Animal],
                                    newPersonInstructions: List[NewPersonInstruction]) extends GameMessage

object GameMessage {

  val classes: List[Class[_]] =
    List(
      classOf[Movements], classOf[PrincipalAction], classOf[Deployments], classOf[ToDeploys],
      classOf[PrincipalPhasePreProcess], classOf[PersonMovement], classOf[BoatMovement],
      classOf[ActionResult], classOf[PersonToActionResult], classOf[NewPersonInstruction],
      classOf[DeployBoat], classOf[DeployedPerson], classOf[DeployedBoat]
    ) ::: Location.classes

}