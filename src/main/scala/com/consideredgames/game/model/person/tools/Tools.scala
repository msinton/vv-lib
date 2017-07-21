package com.consideredgames.game.model.person.tools

import com.consideredgames.common.Utils
import com.consideredgames.game.model.exceptions.ResourceInvalidException
import com.consideredgames.game.model.info.person.skill.Skills.SkillType
import com.consideredgames.game.model.person.tools.ToolInfo._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success}

/**
 * Contains all the Tools for a game. Loads them up from a file
 * Creation throws exception if tools file invalid.
 *
 * Created by matt on 22/02/15.
 */
class Tools(toolsFile: Option[String] = None,
            interchangeablesFile: Option[String] = None,
            toolInfo: Option[List[ToolInfo]] = None) {

  private val importedTools: List[ToolInfo] = toolInfo getOrElse initTools(toolsFile getOrElse Tools.defaultToolsFile)

  val tools: Seq[RichToolInfo] = initRichTools(importedTools)

  val toolsByName: collection.Map[String, RichToolInfo] = {
    val m = mutable.AnyRefMap.empty[String, RichToolInfo]
    for (tool <- tools) {
      m.put(tool.name, tool)
    }
    m
  }

  val interchangeables: List[InterchangeSets] = initInterchangeables(interchangeablesFile getOrElse Tools.defaultInterchangesFile)

  /** For quick retrieval of core tools for each skill. */
  val core: collection.Map[SkillType, Set[RichToolInfo]] = Utils.createInverseMapping(tools) { t: RichToolInfo => t.core.getOrElse(Set())}

  /** For quick retrieval of tools that give a bonus for each skill. */
  val bonuses: collection.Map[SkillType, Set[RichToolInfo]] = Utils.createInverseMapping(tools) { t: RichToolInfo => t.bonuses.keys}

  /** For quick retrieval of tools that are created by each skill. */
  val builders: collection.Map[SkillType, Set[RichToolInfo]] = Utils.createInverseMapping(tools) { t: RichToolInfo => t.builders.map(_.s)}

  for (t <- tools) {
    t.setupMadeFromTools(this)
  }

  private def initTools(toolsFile: String): List[ToolInfo] = {
    val toolsTry = importTools(toolsFile)

    toolsTry match {
      case Success(toolsImported) =>
        toolsImported
      case Failure(e) =>
        throw ResourceInvalidException("could not create tools: " + e.getMessage, e)
    }
  }

  private def initInterchangeables(file: String): List[InterchangeSets] = {
    val toolSet = Interchanges.importInterchanges(Tools.defaultInterchangesFile)

    toolSet match {
      case Success(interchanges) =>
        interchanges map { inter =>
          val toolSet = {
            for {
              toolName <- inter.set
              tool <- toolsByName.get(toolName)
            } yield tool
          }
          InterchangeSets(toolSet.toSet, inter.skills)
        }

      case Failure(e) =>
        throw new ResourceInvalidException("could not create interchangeable set of tools: " + e.getMessage, e)
    }
  }

  private def initRichTools(tools: Seq[ToolInfo]): Seq[RichToolInfo] = {

    val richTools = new ArrayBuffer[RichToolInfo]()

    for (tool <- tools) {
      validate(tool, tools)
      richTools append new RichToolInfo(tool)
    }
    // TODO sorted?
    richTools.sorted
  }

  private def validate(toolInfo: ToolInfo, tools: Seq[ToolInfo]) = {

    // check the tools it is made from are present
    for (optTools <- toolInfo.production.tools) {
      optTools.foreach { case (toolName, n) =>
        tools.find(aTool => aTool.name == toolName).getOrElse(throw ResourceInvalidException(s"Could not find tool that this tool depends on: '$toolName'"))
      }
    }
  }

}

object Tools {

  val defaultToolsFile = "/tools.json"

  val defaultInterchangesFile = "/toolSets.json"

  def apply(): Tools = new Tools()

  def apply(toolsFile: String): Tools = new Tools(Option(toolsFile))

  def apply(toolsFile: String, interchangeablesFile: String): Tools = new Tools(Option(toolsFile), Option(interchangeablesFile))

  def apply(toolInfo: List[ToolInfo]): Tools = new Tools(toolInfo = Option(toolInfo))
}
