package com.consideredgames.game.model.animals

import org.json4s.native.Serialization._
import org.scalatest.{FunSuite, TryValues}

/**
 * Created by matt on 27/08/15.
 */
class AnimalInfoTest extends FunSuite with TryValues {

  test("that can read in from json directly") {
    val animalInfos = AnimalInfo.importFromFile().get

    implicit val formats = AnimalInfo.formats

    val json = write(animalInfos)

    // method under test
    val result = AnimalInfo.readFromString(json)
    
    assert(result.isSuccess)
    assert(result.success.value == animalInfos)
  }
}
