package com.consideredgames.message

import com.consideredgames.game.model.player.PlaceholderPlayer
import com.consideredgames.game.model.player.PlayerColours.{Black, Blue}
import com.consideredgames.message.Messages.{Join, NewGameOptions, NewGameReady, NewGameRequest}
import org.scalatest.{FunSuite, TryValues}

/**
  * Created by matt on 15/07/17.
  */
class MessageMapperTest extends FunSuite with TryValues {

  test("Join") {
    val m = Join(Blue)
    val json = MessageMapper.toJson(m)
    val result = MessageMapper.deJsonify(json)
    assert(result == m)
  }

  test("NewGameReady") {
    val m = NewGameReady(
      "game-id",
      List(PlaceholderPlayer("bob", Blue), PlaceholderPlayer("fred", Black)),
      1L,
      NewGameOptions()
    )

    val json = MessageMapper.toJson(m)
    val result = MessageMapper.deJsonify(json)
    assert(result == m)
  }

  test("NewGameRequest") {
    val m = NewGameRequest(myColour=Blue)
    val json = MessageMapper.toJson(m)
    val result = MessageMapper.deJsonify(json)
    assert(result == m)
  }

}
