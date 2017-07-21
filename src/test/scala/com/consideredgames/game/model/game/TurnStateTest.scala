package com.consideredgames.game.model.game

import com.consideredgames.game.model.game.Phases._
import org.scalatest.FunSuite

/**
 * Created by matt on 08/10/15.
 */
class TurnStateTest extends FunSuite {

  test("creates from turnData correctly") {
    val turnData = TurnData(List("1","2","3"), List(Deployment, Principal, Movement), 3)
    val state = TurnState(turnData)

    assert(state.currentPlayers === List("1"))
    assert(state.nextPlayers === List("2","3"))
    assert(state.phase === Deployment)
    assert(state.nextPhases === List(Principal, Movement))
    assert(state.round === 1)
  }

  test("progresses correctly") {
    val turnData = TurnData(List("1","2"), List(Deployment, Principal), 3)
    var state = TurnState(turnData)

    state = state.turnCompleted

    assert(state.currentPlayers === List("2"))
    assert(state.nextPlayers === List())
    assert(state.phase === Deployment)
    assert(state.nextPhases === List(Principal))
    assert(state.round === 1)

    state = state.turnCompleted

    assert(state.currentPlayers === List("1","2"))
    assert(state.nextPlayers === List())
    assert(state.phase === Principal)
    assert(state.nextPhases === List())
    assert(state.round === 1)

    state = state.turnCompleted
    // round 1 complete

    assert(state.currentPlayers === List("1"))
    assert(state.nextPlayers === List("2"))
    assert(state.phase === Deployment)
    assert(state.nextPhases === List(Principal))
    assert(state.round === 2)

    state = state.turnCompleted
    state = state.turnCompleted
    state = state.turnCompleted
    // round 2 complete
    state = state.turnCompleted
    assert(!state.isFinalTurn)
    state = state.turnCompleted
    assert(state.isFinalTurn)
    state = state.turnCompleted

    assert(state.gameFinished)
  }
}
