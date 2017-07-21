package com.consideredgames.message

import com.consideredgames.game.model.player.PlayerColours
import com.consideredgames.message.Messages.Message
import org.json4s.ShortTypeHints
import org.json4s.native.Serialization
import org.json4s.native.Serialization._

/**
 * Created by matt on 17/07/15.
 */
object MessageMapper {

  implicit val formats = Serialization.formats(ShortTypeHints(Messages.classes)) +
    PlayerColours.serializer

  def deJsonify (json: String): Message = {
    read[Message](json)
  }

  def toJson(m: Message): String = {
    write(m)
  }
}
