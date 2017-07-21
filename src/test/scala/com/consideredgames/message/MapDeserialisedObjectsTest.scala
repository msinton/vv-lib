package com.consideredgames.message

import com.consideredgames.game.model.animals.AnimalInfo
import com.consideredgames.game.model.person.Person
import com.consideredgames.game.model.person.tools._
import com.consideredgames.game.model.round.principal._
import com.consideredgames.game.model.player.PlayerColours
import org.scalatest.{FunSuite, OptionValues}

/**
  * Created by matt on 20/04/17.
  */
class MapDeserialisedObjectsTest extends FunSuite with OptionValues {

  val tools = new Tools()

  val toolUtils = new ToolUtils(tools)

  val actions = new Actions(toolUtils, AnimalInfo.importFromFile().get)

  test("maps person from list") {

    val p = Person(1, PlayerColours.Black)
    p.hp = 500

    val toolInfo = tools.tools.head
    val tool = Tool(1)
    val assTools = Map((toolInfo, 1) -> tool)

    val princ = PrincipalAction(List(ActionFulfillment(actions.Fish, PersonActionParameter(Person(1, PlayerColours.Black)))), assTools)

    val mapped = MapDeserialisedObjects.mapPrincipalActions(princ, List(p), List())

    val mappedPerson = mapped.actions.head.actionParameter match {
      case PersonActionParameter(person) => person
      case _ => ???
    }

    assert(mappedPerson.hp === 500)
    assert(mapped.actions.head.action === actions.Fish)
    assert(mapped.assignedTools === assTools)
  }

  test("maps person from list, two person actions") {

    val p1 = Person(1, PlayerColours.Black)
    p1.hp = 500

    val p2 = Person(2, PlayerColours.Black)
    p2.hp = 600

    val princ = PrincipalAction(List(ActionFulfillment(actions.Fish, TwoPersonActionParameter(Person(1, PlayerColours.Black), Person(2, PlayerColours.Black)))), Map())

    val mapped = MapDeserialisedObjects.mapPrincipalActions(princ, List(p2, p1), List())

    val mappedPeople = mapped.actions.head.actionParameter match {
      case TwoPersonActionParameter(person1, person2) => (person1, person2)
      case _ => ???
    }

    assert(mappedPeople._1.hp === 500)
    assert(mappedPeople._2.hp === 600)
  }
}
