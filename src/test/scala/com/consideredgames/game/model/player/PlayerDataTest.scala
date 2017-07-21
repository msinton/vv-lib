package com.consideredgames.game.model.player

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSuite, OptionValues}

import scala.collection.mutable

/**
 * Created by matt on 04/04/15.
 */
class PlayerDataTest extends FunSuite with OptionValues with MockitoSugar {

  test("that adding and removing players maintains the correct ordering and assigns incremental keys") {

    val playerData = new PlayerData(new mutable.LinkedHashMap[Int, PlayerWithPeople])

    val p1 = mock[PlayerWithPeople]
    val p2 = mock[PlayerWithPeople]
    val p3 = mock[PlayerWithPeople]
    val p4 = mock[PlayerWithPeople]
    val p5 = mock[PlayerWithPeople]

    playerData.add(p1)
    assert(playerData.players.last._2 == p1)
    playerData.add(p2)

    playerData.players.remove(0)
    assert(playerData.players.last._2 == p2)

    assert(playerData.getPlayer(0) == None)
    assert(playerData.getPlayer(1).value == p2)
    assert(playerData.numberOfPlayers === 1)
    assert(playerData.getPlayerNumbers == Set(1))

    playerData.add(p3)
    assert(playerData.players.last._2 == p3)

    assert(playerData.getPlayer(0) == None)
    assert(playerData.getPlayer(1).value == p2)
    assert(playerData.getPlayer(2).value == p3)
    assert(playerData.numberOfPlayers === 2)
    assert(playerData.getPlayerNumbers == Set(1,2))

    playerData.add(p4)
    assert(playerData.players.last._2 == p4)

    playerData.players.remove(2)
    playerData.add(p5)
    assert(playerData.players.last._2 == p5)
    assert(playerData.players.last._1 == 4)

    val itr = playerData.players.iterator

    assert(itr.next()._2 == p2)
    assert(itr.next()._2 == p4)
    assert(itr.next()._2 == p5)
    assert(!itr.hasNext)
  }

  test("create from another player data, resets the keys to be in order") {

    val playerData = new PlayerData(new mutable.LinkedHashMap[Int, PlayerWithPeople])

    val p1 = mock[PlayerWithPeople]
    val p2 = mock[PlayerWithPeople]
    val p3 = mock[PlayerWithPeople]

    playerData.add(p1)
    playerData.add(p2)
    playerData.add(p3)

    playerData.players.remove(1)

    val newPlayerData = PlayerData(playerData)

    assert(newPlayerData.getPlayer(0).value === p1)
    assert(newPlayerData.getPlayer(1).value === p3)
  }
}
