package com.consideredgames.game.state

import com.consideredgames.message.Messages.NewGameReady

/**
  * Created by matt on 24/07/17.
  * To fix type erasure
  */
case class ReadyGames(games: List[NewGameReady])
