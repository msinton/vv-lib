package com.consideredgames.game.model.person.tools

import com.consideredgames.common.Importer
import com.consideredgames.game.model.animals.AnimalInfo
import com.consideredgames.game.model.info.person.skill.Skills
import com.consideredgames.game.model.info.person.skill.Skills.SkillType
import com.consideredgames.game.model.resources.{ResourceGroup, Resources}
import com.consideredgames.serializers.Named
import org.json4s.DefaultFormats

import scala.util.Try

/**
 * Created by matt on 27/02/15.
 */
case class ToolInfo(
                     name: String,
                     startLife: Int,
                     worth: Int,
                     bonuses: List[SkillToInt],
                     production: Production,
                     builders: List[Builder],
                     core: Option[List[SkillType]]
                     ) extends Named

case class SkillToInt(s: SkillType, n: Int)

/** The skill (crafting) that can create the tool and the minimum level that skill needs to be to do so. */
case class Builder(s: SkillType, level: Option[Int])

/**
 * @param resources what it needs to be created
 * @param tools what tools it needs to be created
 * @param produces how many are created (defaults to 1)
 */
case class Production(resources: List[ResourceGroup], tools: Option[Map[String, Int]], produces: Option[Int])

object ToolInfo {

  implicit val formats = DefaultFormats + Resources.serializer + Skills.serializer

  def importTools(toolsFile: String): Try[List[ToolInfo]] = Importer.importList[ToolInfo](toolsFile)

  def readFromString(json: String): Try[List[ToolInfo]] = Importer.readList[ToolInfo](json)
}