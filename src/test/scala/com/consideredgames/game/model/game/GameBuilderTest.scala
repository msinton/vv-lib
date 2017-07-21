package com.consideredgames.game.model.game

import com.consideredgames.game.model.animals.AnimalInfo
import com.consideredgames.game.model.board.BoardData
import com.consideredgames.game.model.person.tools.{ToolUtils, Tools}
import com.consideredgames.game.model.player.PlayerColours._
import com.consideredgames.game.model.player._
import org.scalatest.FunSuite

/**
 * Created by matt on 12/09/15.
 */
class GameBuilderTest extends FunSuite {

  val animalInfos = AnimalInfo.importFromFile().get
  val tools = new Tools()
  val newGameConfig = NewGameConfig(List(PlaceholderPlayer("Bob", Black), PlaceholderPlayer("Fred", Blue)), 123L, animalInfos, tools, List())

  test("builds a game with a boardData") {
    val game = GameBuilder.build(newGameConfig, "Bob")

    assert(game.boardUtils.boardData.isInstanceOf[BoardData])
    assert(game.boardUtils.boardData.hexes.nonEmpty)
  }

  test("builds a game with playerData") {
    val game = GameBuilder.build(newGameConfig, "Bob")

    assert(game.playerData.size == 2)
    assert(game.playerData.get("Bob").exists(_.isInstanceOf[FullPlayer]))
    assert(game.playerData.get("Fred").exists(_.isInstanceOf[OtherPlayer]))
  }

  test("builds a game with toolUtils") {
    val game = GameBuilder.build(newGameConfig, "Bob")

    assert(game.toolUtils.isInstanceOf[ToolUtils])
    assert(game.toolUtils.allToolInfo == tools)
  }

  test("builds a game with gameProcessors") {
    val game = GameBuilder.build(newGameConfig, "Bob")
    assert(game.gameProcessors.isInstanceOf[GameProcessors])
  }
}
