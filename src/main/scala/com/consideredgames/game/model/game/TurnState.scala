package com.consideredgames.game.model.game

import com.consideredgames.game.model.game.Phases.{Movement, Phase, Principal, PrincipalEnd}

/**
 * Created by matt on 08/10/15.
 */

case class TurnData(playerOrder: List[String], phaseOrder: List[Phase], rounds: Int)

case class TurnState(data: TurnData, currentPlayers: List[String], nextPlayers: List[String],
                     phase: Phase, nextPhases: List[Phase], round: Int = 1, gameFinished: Boolean = false) {

  private def newRound = {
    (data.phaseOrder.head, data.phaseOrder.tail, round + 1)
  }

  private def newPlayTurn = {
    if (nextPhases.headOption.contains(Principal))
      (data.playerOrder, Nil)
    else if (nextPhases.headOption.contains(PrincipalEnd))
      (Nil, Nil)
    else
      (List(data.playerOrder.head), data.playerOrder.tail)
  }

  private def progressPhase = {
    if (nextPhases.isEmpty)
      newRound
    else
      (nextPhases.head, nextPhases.tail, round)
  }

  def isFinalTurn = {
    nextPlayers.isEmpty && nextPhases.isEmpty && round == data.rounds
  }

  def turnCompleted: TurnState = {
    if (isFinalTurn)
      finishIt
    else if (nextPlayers.isEmpty) {
      val newPhaseData = progressPhase
      val newPlayData = newPlayTurn
      TurnState(data, newPlayData._1, newPlayData._2, newPhaseData._1, newPhaseData._2, newPhaseData._3)
    } else {
      TurnState(data, List(nextPlayers.head), nextPlayers.tail, phase, nextPhases, round)
    }
  }

  private def finishIt =
    TurnState(data,
      currentPlayers,
      nextPlayers,
      phase,
      nextPhases,
      round,
      gameFinished = true)

}

object TurnState {

  def apply(data: TurnData): TurnState = {
    val dummyState = TurnState(data, Nil, Nil, Movement, Nil, 0)
    val newPhaseData = dummyState.progressPhase
    val newPlayData = dummyState.newPlayTurn
    TurnState(data, newPlayData._1, newPlayData._2, newPhaseData._1, newPhaseData._2, newPhaseData._3)
  }
}

