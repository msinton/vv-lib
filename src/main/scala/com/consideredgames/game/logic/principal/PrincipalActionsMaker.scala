package com.consideredgames.game.logic.principal

import com.consideredgames.game.model.hex.Point
import com.consideredgames.game.model.person.Person
import com.consideredgames.game.model.person.tools.{RichToolInfo, Tool}
import com.consideredgames.game.model.resources.ItemContainer
import com.consideredgames.game.model.round.principal.{Action, ActionFulfillment}

/**
 * Created by matt on 13/10/15.
 */
class PrincipalActionsMaker(manager: PrincipalActionsManager, itemContainer: ItemContainer, submitHandler: (List[ActionFulfillment], collection.Map[(RichToolInfo, Int), Tool]) => Unit) {

  def add(action: Action, point: Point) = manager.addAction(action, point)

  def add(action: Action, person: Person) = manager.addAction(action, person)

  def add(action: Action, p1: Person, p2: Person) = manager.addAction(action, p1, p2)

  def add(action: Action) = manager.addAction(action)

  def undo(person: Person) = manager.removeAction(person)

  def undo(point: Point) = manager.removeAction(point)

  def actions = manager.actions

  def points = manager.actionPoints

  def submit() = {
    submitHandler(actions, itemContainer.assignedTools)
  }
}
