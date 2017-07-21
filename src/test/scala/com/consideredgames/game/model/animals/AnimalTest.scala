package com.consideredgames.game.model.animals

import org.json4s.DefaultFormats
import org.json4s.native.Serialization.{read, write}
import org.scalatest.FunSuite

/**
 * Created by matt on 09/03/15.
 */
class AnimalTest extends FunSuite {

  val anims = AnimalInfo.importFromFile()

  test("that can serialize representation") {

    val a = Animal(anims.get(0), female = true)

    implicit val formats = DefaultFormats + AnimalInfo.serializer

    val json = write(a)

    assert(a === read[Animal](json))
  }

  test("that can serialize representation 2") {

    val a = Animal(anims.get(1), female = false, wild = true, Pregnant(2))

    implicit val formats = AnimalInfo.formats

    val json = write(a)

    assert(a === read[Animal](json))
  }

  test("that can serialize with custom serializer") {

    val animalInfos = AnimalInfo.importFromFile().get

    val a = Animal(anims.get(1), female = false, wild = true, Pregnant(2))

    implicit val formats = DefaultFormats + Animal.serializer(animalInfos)

    val json = write(a)

    assert(a === read[Animal](json))
  }

  test("that can serialize with custom serializer 2") {

    val animalInfos = AnimalInfo.importFromFile().get

    val a = Animal(anims.get(1), female = false)

    implicit val formats = DefaultFormats + Animal.serializer(animalInfos)

    val json = write(a)

    assert(a === read[Animal](json))
  }

}
