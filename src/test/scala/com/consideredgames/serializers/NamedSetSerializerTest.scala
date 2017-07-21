package com.consideredgames.serializers

import org.json4s.DefaultFormats
import org.json4s.native.Serialization.{read, write}
import org.scalatest.FunSuite

/**
  * Created by matt on 19/04/17.
  */
class NamedSetSerializerTest extends FunSuite {

  // create a test example for serialization
  object Resources {

    sealed abstract class Resource(val name: String, val worth: Int) extends Named {
    }

    case object Wood extends Resource(name = "wood", worth = 0)

    case object Stone extends Resource(name = "stone", worth = 0)

    val resourcesSet = Set(Wood, Stone)

    val serializer = new NamedSetSerializer[Resource](resourcesSet, Some("res"))
  }

  case class TestObjResource(list: List[Resources.Resource])

  test("serializePolymorphicObjectList") {

    implicit val formats = DefaultFormats + Resources.serializer

    val resources = TestObjResource(List(Resources.Stone, Resources.Wood))

    val json = write[TestObjResource](resources)

    assert(read[TestObjResource](json).list == resources.list)
  }
}
