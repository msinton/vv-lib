package com.consideredgames.game.model.game

/**
  * Created by matt on 19/04/17.
  */
case class GameState(var turnState: TurnState) {

  def endTurn() = turnState = turnState.turnCompleted


}
