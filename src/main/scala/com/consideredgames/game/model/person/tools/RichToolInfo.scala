package com.consideredgames.game.model.person.tools

import com.consideredgames.game.model.info.person.skill.Skills.SkillType
import com.consideredgames.game.model.resources.Resources.Resource

import scala.collection.mutable

/**
 * Created by matt on 27/02/15.
 */
case class RichToolInfo(self: ToolInfo) extends Comparable[RichToolInfo] {

  val bonuses: collection.Map[SkillType, Int] = {

    val m = mutable.AnyRefMap.empty[SkillType, Int]

    for (bonus <- self.bonuses) {
      m.put(bonus.s, bonus.n)
    }
    m
  }

  val madeFromResources: collection.Map[Resource, Int] = {

    val m = mutable.AnyRefMap.empty[Resource, Int]

    for (res <- self.production.resources) {
      m.put(res.r, res.n.toInt)
    }
    m
  }

  private var madeFromTools_ : collection.Map[RichToolInfo, Int] = collection.Map()

  def setupMadeFromTools(tools: Tools): Unit = {

    val m = mutable.AnyRefMap.empty[RichToolInfo, Int]

    for {
      toolOpt <- self.production.tools
      (toolName, n) <- toolOpt
      t <- tools.toolsByName.get(toolName)
    } m.put(t, n)

    madeFromTools_ = m
  }

  def madeFromTools = madeFromTools_

  // for convenience
  def name = self.name

  def worth = self.worth

  def startLife = self.startLife

  def core = self.core

  def builders = self.builders

  def production = self.production

  override def compareTo(o: RichToolInfo): Int = {
    worth - o.worth match {
      case x if x == 0 => name compareTo o.name
      case y => y
    }
  }
}

object RichToolInfo {

  /** The ordering is for lowest skill bonus first. */
  def orderingBySkillBonus(s: SkillType): Ordering[RichToolInfo] = Ordering.by(e => e.bonuses.getOrElse(s, Int.MinValue))
}
