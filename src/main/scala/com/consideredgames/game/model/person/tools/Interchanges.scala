package com.consideredgames.game.model.person.tools

import com.consideredgames.common.Importer
import com.consideredgames.game.model.info.person.skill.Skills
import com.consideredgames.game.model.info.person.skill.Skills.SkillType
import org.json4s.DefaultFormats

import scala.util.Try

/**
 * Created by matt on 12/03/15.
 */
case class Interchanges(set: List[String], skills: Option[List[SkillType]])

case class InterchangeSets(set: Set[RichToolInfo], skills: Option[List[SkillType]])

object Interchanges {

  implicit val formats = DefaultFormats + Skills.serializer

  def importInterchanges(file: String): Try[List[Interchanges]] = Importer.importList[Interchanges](file)
}