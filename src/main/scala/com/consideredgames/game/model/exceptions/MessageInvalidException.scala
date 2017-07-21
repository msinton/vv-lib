package com.consideredgames.game.model.exceptions

/**
 * Created by matt on 27/02/15.
 */
case class MessageInvalidException(s: String = null, exception: Throwable = null) extends RuntimeException(s, exception)
