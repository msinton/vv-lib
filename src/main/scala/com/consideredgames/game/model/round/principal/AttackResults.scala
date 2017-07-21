package com.consideredgames.game.model.round.principal

import com.consideredgames.game.model.person.Person
import com.consideredgames.game.model.player.PlayerColours.PlayerColour

/**
 * Created by matt on 10/06/15.
 */
case class Damage(amount: Int, from: Person, to: Person)

case class AttackResults(damages: List[Damage], deaths: collection.Map[PlayerColour,Set[Person]])
