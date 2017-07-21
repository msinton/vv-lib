package com.consideredgames.game.event

/**
  * Created by matt on 18/07/17.
  */
sealed trait GameActivityEvent extends Event

case class StartGame(gameId: String) extends GameActivityEvent