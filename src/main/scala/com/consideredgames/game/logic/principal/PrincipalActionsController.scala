package com.consideredgames.game.logic.principal

import com.consideredgames.game.model.game.GameState
import com.consideredgames.game.model.person.tools.{RichToolInfo, Tool}
import com.consideredgames.game.model.round.principal.ActionFulfillment
import com.consideredgames.game.model.player.{FullPlayer, PlayerWithPeople}
import com.consideredgames.message.PrincipalAction

/**
 * Created by matt on 13/10/15.
 */
case class PrincipalActionsController(player: FullPlayer,
                                 gameState: GameState,
                                 playerData: Map[String, PlayerWithPeople],
                                 processor: PrincipalActionsProcessor) {

  val manager = PrincipalActionsManager()

  val submitHandler: (List[ActionFulfillment], collection.Map[(RichToolInfo, Int), Tool]) => Unit = (actions, assignedTools) => {
    //TODO send message to server
    val message = PrincipalAction(actions, assignedTools)
    gameState.endTurn()
  }

  def getActionMaker: PrincipalActionsMaker = {
    new PrincipalActionsMaker(manager, player.itemContainer, submitHandler)
  }
}

