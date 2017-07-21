package com.consideredgames.utils

import java.nio.file.{Files, Path, Paths}

import org.scalatest.{BeforeAndAfterEach, FunSuite}

import scala.util.Failure

/**
  * Created by matt on 14/04/17.
  */
class LoadAndSaveTest extends FunSuite with BeforeAndAfterEach {

  val testFilename = "test-load-and-save"

  override def afterEach() {
    val path: Path = Paths.get(System.getProperty("user.home"), testFilename)
    Files.deleteIfExists(path)
  }

  test("testWriteToCSV") {

    val values = List("Hi", "Bob")
    val values2 = List("How", "are you?")
    val path: Path = Paths.get(System.getProperty("user.home"), testFilename)
    LoadAndSave.writeToCSV(path, ",", values)
    LoadAndSave.writeToCSV(path, ",", values2)

    val strings = Files.readAllLines(path)
    assert(strings.get(0) == "Hi,Bob")
    assert(strings.get(1) == "How,are you?")
  }

  test("testReadFromCSVToSeq") {

    val values = List("Hi", "Bob ")
    val values2 = List(" ")
    val path: Path = Paths.get(System.getProperty("user.home"), testFilename)
    LoadAndSave.writeToCSV(path, ",", values)
    LoadAndSave.writeToCSV(path, ",", values2)
    val result = LoadAndSave.readFromCSVToSeq(path.toString, ",")

    assert(result == List(List("Hi", "Bob")))
  }

  test("testSafeReadFromCSVToSeq when exception") {

    val path: Path = Paths.get(System.getProperty("user.home"), "not-a-file")
    val result = LoadAndSave.safeReadFromCSVToSeq(path.toString, ",")

    assert(result.isFailure)
  }
}
