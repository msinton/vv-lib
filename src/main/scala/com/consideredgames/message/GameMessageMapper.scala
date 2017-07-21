package com.consideredgames.message

import com.consideredgames.game.model.animals.{Animal, AnimalInfo}
import com.consideredgames.game.model.game.Phases
import com.consideredgames.game.model.info.person.skill.Skills
import com.consideredgames.game.model.person.tools.{RichToolInfo, Tools}
import com.consideredgames.game.model.player.PlayerColours
import com.consideredgames.game.model.player.PlayerColours.PlayerColour
import com.consideredgames.game.model.resources.Resources
import com.consideredgames.game.model.round.principal.ActionResults
import com.consideredgames.game.model.season.Seasons
import org.json4s.native.Serialization
import org.json4s.native.Serialization._
import org.json4s.{CustomKeySerializer, MappingException, ShortTypeHints}

/**
 * Created by matt on 26/06/15.
 */
class GameMessageMapper(val animalInfos: List[AnimalInfo], val tools: Tools) {

  val playerColourStrings: Set[String] = PlayerColours.playerColoursSet map {_.name}

  object playerColourCustomKeySerializer extends CustomKeySerializer[PlayerColour](
    format => ( {
      case str: String if playerColourStrings.contains(str) =>
        PlayerColours.playerColoursSet.find(_.name == str).getOrElse(throw new MappingException(s"Can't convert $str to a player colour"))
    }, {
      case x: PlayerColour => x.name
    })
  )

  object assignedToolsCustomKeySerializer extends CustomKeySerializer[(RichToolInfo, Int)](
    format => ( {
      case str: String if tools.toolsByName.get(str.split("-").head).nonEmpty =>
        val splits = str.split("-")
        val tool = tools.toolsByName.getOrElse(splits.head, throw new MappingException(s"Can't convert $str to a assignedToolKey"))
        (tool, splits(1).toInt)
    }, {
      case (t: RichToolInfo, n: Int) => t.name + "-" + n
    })
  )

  implicit val formats = Serialization.formats(ShortTypeHints(GameMessage.classes ::: ActionResults.classes)) +
    Skills.serializer +
    Resources.serializer +
    PlayerColours.serializer +
    playerColourCustomKeySerializer +
    Seasons.serializer +
    Animal.serializer(animalInfos) +
    Phases.serializer +
    assignedToolsCustomKeySerializer

  def toJson(m: GameMessage): String = write(m)

  def toMessage(str: String): GameMessage = read[GameMessage](str)
}
