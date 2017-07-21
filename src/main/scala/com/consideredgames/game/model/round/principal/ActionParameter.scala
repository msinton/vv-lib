package com.consideredgames.game.model.round.principal

import com.consideredgames.game.model.hex.Point
import com.consideredgames.game.model.person.Person

/**
 * Created by matt on 19/05/15.
 */
sealed trait ActionParameter

case class EmptyActionParameter() extends ActionParameter

case class PointActionParameter(point: Point) extends ActionParameter

case class PersonActionParameter(person: Person) extends ActionParameter

case class TwoPersonActionParameter(p1: Person, p2: Person) extends ActionParameter
