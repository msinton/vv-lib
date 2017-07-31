package com.consideredgames.game.event

/**
  * Created by matt on 18/07/17.
  */
sealed trait ConnectionEvent extends Event

case object Disconnected extends ConnectionEvent
case object Connected extends ConnectionEvent

case object ConnectAttempt extends ConnectionEvent