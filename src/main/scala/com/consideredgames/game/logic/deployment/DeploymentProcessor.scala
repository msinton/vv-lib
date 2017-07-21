package com.consideredgames.game.logic.deployment

import com.consideredgames.game.model.board.BoardUtils
import com.consideredgames.game.model.deployment.{BorderLocation, HexLocation}
import com.consideredgames.game.model.hex.{Boat, Hex, Side}
import com.consideredgames.game.model.person.{NewPersonInstruction, Person}
import com.consideredgames.game.model.player.PlayerWithPeople
import com.consideredgames.message.DeployedPerson

/**
 * Process deployments
 */
case class DeploymentProcessor(boardUtils: BoardUtils) {

  def deployBoat(hex: Hex, side: Side) = {
    val boat = Boat(hex, side)
    deploy(boat)
    boat
  }

  def deploy(people: List[NewPersonInstruction], playerWithPeople: PlayerWithPeople) {
    people.foreach { instr =>

      val person = playerWithPeople.create(instr)
      instr.location.foreach {

        case HexLocation(hex) =>
          boardUtils.mapToGameElement(hex).foreach { h =>
            deploy(person, h)
          }

        case BorderLocation(hex, side) =>
          boardUtils.mapToGameElement(hex).foreach { h =>
            h.boats.get(side).foreach { boat =>
              deploy(person, boat)
            }
          }
      }
    }
  }

  // deployments done by others
  def doDeploy(people: List[DeployedPerson], playerWithPeople: PlayerWithPeople): Unit = {
    people.foreach { deployedPerson =>
      deployedPerson.location match {
        case HexLocation(h) =>
          boardUtils.mapToGameElement(h).foreach { hex =>

            //the person should already have been created on the server
            playerWithPeople.person(deployedPerson.person.id).foreach { person =>
              deploy(person, hex)
            }
          }

        case BorderLocation(hex, side) =>
          boardUtils.mapToGameElement(hex).foreach { h =>
            h.boats.get(side).foreach { boat =>
              playerWithPeople.person(deployedPerson.person.id).foreach { person =>
                deploy(person, boat)
              }
            }
          }
      }
    }
  }

  def deploy(person: Person, hex: Hex) = {
    hex.person = Option(person)
    person.hex = Option(hex)
  }

  def deploy(person: Person, boat: Boat): Unit = {
    boat.addPerson(person)
    person.boat = Option(boat)
    boardUtils.boardData.add(boat)
  }

  def deploy(boats: List[Boat]): Unit = {
    boats.foreach { boat =>
      val hex = boardUtils.mapToGameElement(boat.hexA)
      hex.foreach { h =>

        val realBoat = Boat(h, boat.sideA)
        deploy(realBoat)
      }
    }
  }

  def deploy(boat: Boat): Unit ={
    boardUtils.boardData.add(boat)
  }

  def valid(people: List[NewPersonInstruction], playerWithPeople: PlayerWithPeople): (Boolean, List[Boolean]) = {
    val results = people.map { instr =>
      playerWithPeople.canAddPerson(instr.id, instr.playerColour) && instr.location.forall {

        case HexLocation(hex) =>
          boardUtils.mapToGameElement(hex).nonEmpty

        case BorderLocation(hex, side) =>
          boardUtils.mapToGameElement(hex).fold(false) {
            _.boats.get(side).fold(false)(_.canAddPerson(Person(instr)))
          }

        case _ => false
      }
    }
    (results.forall(_ == true), results)
  }

  def validDeploy(deployedPeople: List[DeployedPerson], playerWithPeople: PlayerWithPeople): (Boolean, List[Boolean]) = {
    val results = deployedPeople.map { deployedPerson =>
      deployedPerson.location match {
        case HexLocation(h) => boardUtils.mapToGameElement(h).exists(valid(deployedPerson.person, _))

        case BorderLocation(hex, side) =>
          boardUtils.mapToGameElement(hex).fold(false)(valid(deployedPerson.person, _, side))

        case _ => false
      }
    }
    (results.forall(_ == true), results)
  }

  def valid(boats: List[Boat]): (Boolean, List[Boolean]) = {
    val results = boats.map { boat =>
      val hex = boardUtils.mapToGameElement(boat.hexA)
      hex.exists(boardUtils.isAvailableForBoat(_, boat.sideA))
    }

    (results.forall(_ == true), results)
  }

  def valid(person: Person, hex: Hex) = {
    hex.person.isEmpty && hex.neighbours.exists{case(side, h) => h.person.exists(_.playerColour == person.playerColour)}
  }

  def valid(person: Person, hex: Hex, side: Side) = {
    hex.boats.get(side).fold(false)(_.canAddPerson(person))
  }

}
