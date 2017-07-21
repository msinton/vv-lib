package com.consideredgames.utils

import java.io.FileNotFoundException
import java.nio.file.{Files, Path, StandardOpenOption}

import com.typesafe.scalalogging.{LazyLogging, Logger}

import scala.io.Source
import scala.util.{Failure, Success, Try}

/**
  * Created by matt on 14/04/17.
  */
object LoadAndSave extends LazyLogging {

  /**
    * Appends the list of values as a string
    * - with each value separated by the separator -
    * to a file specified by the path.
    * Creates the file if does not already exist, o/w appends to the file.
    */
  def writeToCSV(path: Path, separator: String, values: Seq[String]) {
    val toWrite = values.mkString(separator).concat(System.lineSeparator())
    Files.write(path, toWrite.getBytes, StandardOpenOption.APPEND, StandardOpenOption.CREATE)
  }

  /**
    * The file should contain lines separated by the separator provided.
    *
    * @param separator For example a comma.
    */
  def readFromCSVToSeq(filename: String, separator: String): Seq[Seq[String]] = {
    Source.fromFile(filename).getLines()
      .map(_.split(",").map(_.trim).toList)
      .filterNot(_.forall(_.isEmpty))
      .toList
  }

  def safeReadFromCSVToSeq(filename: String, separator: String): Try[Seq[Seq[String]]] = {
    try {
      Success(readFromCSVToSeq(filename, separator))
    } catch {
      case ex: FileNotFoundException => {
        logger.warn(s"could not read file: $filename, ex: $ex")
        Failure(ex)
      }
      case ex: Throwable => {
        logger.warn(s"could not read file: $filename, ex: $ex")
        Failure(ex)
      }
    }
  }
}
