package com.consideredgames.message

import com.consideredgames.game.model.exceptions.MessageInvalidException
import com.consideredgames.game.model.hex.Point
import com.consideredgames.game.model.person.Person
import com.consideredgames.game.model.round.principal._

/**
 * Created by matt on 15/07/15.
 */
object MapDeserialisedObjects {

  /**
    * Maps the objects from inside the principal action to the corresponding objects in the supplied lists
    */
  def mapPrincipalActions(principalAction: PrincipalAction, people: List[Person], points: List[Point]): PrincipalAction = {

    val mappedActions = principalAction.actions.map {

      case ActionFulfillment(action, param: PersonActionParameter) =>
        val person = people.find(_ == param.person).getOrElse(throw MessageInvalidException(s"Could not find person in $param"))
        ActionFulfillment(action, PersonActionParameter(person))

      case ActionFulfillment(action, param: TwoPersonActionParameter) =>
        val p1 = people.find(_ == param.p1).getOrElse(throw MessageInvalidException(s"Could not find person in $param"))
        val p2 = people.find(_ == param.p2).getOrElse(throw MessageInvalidException(s"Could not find person in $param"))
        ActionFulfillment(action, TwoPersonActionParameter(p1, p2))

      case ActionFulfillment(action, param: PointActionParameter) =>
        val point = points.find(_ == param.point).getOrElse(throw MessageInvalidException(s"Could not find point in $param"))
        ActionFulfillment(action, PointActionParameter(point))

      case x@ActionFulfillment(_, _: EmptyActionParameter) => x
    }

    PrincipalAction(mappedActions, principalAction.assignedTools)
  }
}
