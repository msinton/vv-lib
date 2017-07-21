package com.consideredgames.game.model.info.person.skill

import com.consideredgames.game.model.person.Person
import com.consideredgames.serializers.{Named, NamedSetSerializer}

/**
 * Created by matt on 27/02/15.
 */
object Skills {

  sealed abstract class SkillType(
                                   val name: String,
                                   val maxLevel: Int, //The max level that can be obtained for the skill, including bonuses from tools
                                   val xpLevels: List[Int],
                                   val permanent: Boolean)  //The xp required to reach each level. The first value is the xp required to reach level 2.
    extends Named {
  }

  sealed abstract class CrafterSkillType(name: String, maxLevel: Int, xpLevels: List[Int], val capacities: List[Int]) extends SkillType(name, maxLevel, xpLevels, false) {
  }

  case object Mason extends CrafterSkillType("mason", 12, List(1,1,1,1,1,2,2,3), capacities = List(1,2,3,4,5,5,6,6,7,7,8,8))

  case object Lumberjack extends SkillType("lumberjack", 12, List(1,1,1,1,1,2,2,3), true)

  case object Miner extends SkillType("miner", 12, List(1,1,1,1,1,2,2,3), true)

  case object Fisherman extends SkillType("fisherman", 12, List(1,1,1,1,1,2,2,3), true)

  case object Reproducer extends SkillType("reproducer", 7, List(1,2,2,3), false)

  case object AgricultureFarmer extends SkillType("agriculture farmer", 12, List(1,1,1,1,1,2,2,3), false)

  case object BoarFarmer extends SkillType("boar farmer", 1, List(1,1,1,1,1,2,2,3), false)

  // The skills with xpLevels=emptyList are not level-able, only tools affect them
  case object HorseFarmer extends SkillType("horse farmer", 1, List(), false)

  case object RabbitFarmer extends SkillType("rabbit farmer", 1, List(), false)

  case object ArableFarmer extends SkillType("arable farmer", 12, List(1,1,1,1,1,2,2,3), false)

  case object Attacker extends SkillType("attacker", 12, List(1,1,1,1,1,2,2,3), false)

  case object Defender extends SkillType("defender", 12, List(1,1,1,1,1,2,2,3), false)

  case object Carpenter extends CrafterSkillType("carpenter", 12, List(1,1,1,1,1,2,2,3), capacities = List(1,2,3,4,5,5,6,6,7,7,8,8))

  case object Ironmonger extends CrafterSkillType("ironmonger", 12, List(1,1,1,1,1,2,2,3), capacities = List(1,2,3,4,5,5,6,6,7,7,8,8))

  case object Goldsmith extends CrafterSkillType("goldsmith", 7, List(1,2,2,3), capacities = List(1,2,3,3,4,4,5))

  case object Potter extends CrafterSkillType("potter", 10, List(1,1,1,2,2,2,3), capacities = List(1,2,3,4,5,5,6,6,7,8))

  case object Tailor extends CrafterSkillType("tailor", 7, List(1,2,2,3), capacities = List(1,2,3,3,4,4,5))

  case object Cook extends SkillType("cook", 7, List(1,2,2,3), false)

  case object Unskilled extends CrafterSkillType("unskilled", 0, List(), capacities = List(1))

  val skillTypesSet = Set(Mason, Miner, Lumberjack, Fisherman, Reproducer, AgricultureFarmer, BoarFarmer, HorseFarmer, RabbitFarmer,
    ArableFarmer, Attacker, Defender, Carpenter, Ironmonger, Goldsmith, Potter, Tailor, Cook, Unskilled)

  val skillTypes: collection.immutable.Seq[SkillType] = skillTypesSet.toList.sorted

  val serializer = new NamedSetSerializer[SkillType](skillTypesSet, Some("skill"))

  /** If at last xpLevel or above, then XP to next level is the same as the last one. */
  def getXPNeededToLevelUp(currentXP: Int, skill: SkillType): Int = {

    var acc = 0
    var i = 0
    while(acc <= currentXP) {

      if (skill.xpLevels.size < i+1) {
        acc += skill.xpLevels.last
      } else {
        acc += skill.xpLevels(i)
      }
      i += 1
    }

    acc - currentXP
  }

  def skillLevel(xp: Int, skillType: SkillType): Int = {
    var acc = 0
    var i = 0
    while(acc < xp) {
      if (skillType.xpLevels.size < i+1) {
        acc += skillType.xpLevels.last
      } else {
        acc += skillType.xpLevels(i)
      }
      i += 1
    }
    i
  }

  def skillLevel(person: Person, skillType: SkillType): Int = {
    skillLevel(person.skills.get(skillType).fold(0)(_.xpLevel), skillType)
  }

}
