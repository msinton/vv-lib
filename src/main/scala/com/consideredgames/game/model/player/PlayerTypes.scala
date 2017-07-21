package com.consideredgames.game.model.player

import com.consideredgames.game.model.person.{NewPersonInstruction, Person}
import com.consideredgames.game.model.resources.ItemContainer
import com.consideredgames.game.model.player.PlayerColours.PlayerColour

import scala.collection.mutable


trait Player {
  def name: String
  def colour: PlayerColour
}

trait PlayerWithPeople extends Player {

  val people = mutable.LongMap.empty[Person]

  private var personCount = 0

  def create(personInstructions: NewPersonInstruction) = {
    personCount += 1
    val p = Person(personInstructions)
    people.put(personInstructions.id, p)
    p
  }

  def create(person: Person) = {
    personCount += 1
    people.put(person.id, person)
    person
  }

  def person(personId: Int) = {
    people.get(personId)
  }

  def kill(personId: Int): Option[Person] = {
    people.remove(personId)
  }

  def kill(person: Person): Option[Person] = {
    kill(person.id)
  }

  def canAddPerson(personId: Int, colourToAdd: PlayerColour) = {
    colourToAdd == colour && people.get(personId).isEmpty
  }
}

case class PlaceholderPlayer(name: String, colour: PlayerColour) extends Player
case class FullPlayer (name: String, itemContainer : ItemContainer, colour: PlayerColour) extends PlayerWithPeople
case class OtherPlayer (name: String, colour: PlayerColour) extends PlayerWithPeople
