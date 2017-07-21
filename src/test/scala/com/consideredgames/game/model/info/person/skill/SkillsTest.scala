package com.consideredgames.game.model.info.person.skill

import com.consideredgames.game.model.info.person.skill.Skills.Carpenter
import com.consideredgames.game.model.person.Person
import com.consideredgames.game.model.player.PlayerColours.Black
import org.scalatest.FunSuite

class SkillsTest extends FunSuite {

  test("skillLevel") {
    assert(Skills.skillLevel(1, Carpenter) === 1)
    assert(Skills.skillLevel(2, Carpenter) === 2)
    assert(Skills.skillLevel(3, Carpenter) === 3)
    assert(Skills.skillLevel(4, Carpenter) === 4)
    assert(Skills.skillLevel(5, Carpenter) === 5)
    assert(Skills.skillLevel(6, Carpenter) === 6)
    assert(Skills.skillLevel(7, Carpenter) === 6)
    assert(Skills.skillLevel(8, Carpenter) === 7)
    assert(Skills.skillLevel(9, Carpenter) === 7)
    assert(Skills.skillLevel(10, Carpenter) === 8)
    assert(Skills.skillLevel(11, Carpenter) === 8)
    assert(Skills.skillLevel(12, Carpenter) === 8)
    assert(Skills.skillLevel(13, Carpenter) === 9)
    assert(Skills.skillLevel(14, Carpenter) === 9)
    assert(Skills.skillLevel(15, Carpenter) === 9)
    assert(Skills.skillLevel(16, Carpenter) === 10)
  }

  test("xp needed to lvl up") {
    assert(Skills.getXPNeededToLevelUp(0, Carpenter) === 1)
    assert(Skills.getXPNeededToLevelUp(7, Carpenter) === 2)
    assert(Skills.getXPNeededToLevelUp(15, Carpenter) === 3)
    assert(Skills.getXPNeededToLevelUp(16, Carpenter) === 2)
    assert(Skills.getXPNeededToLevelUp(18, Carpenter) === 3)
  }

  test("skillLevel person") {
    val p = Person(1, playerColour = Black)
    assert(Skills.skillLevel(p, Carpenter) == 0)

    p.skills.update(Carpenter, SkillStat(1))
    assert(Skills.skillLevel(p, Carpenter) == 1)
  }
}
