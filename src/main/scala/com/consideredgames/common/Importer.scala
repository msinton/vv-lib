package com.consideredgames.common

import com.consideredgames.game.model.exceptions.ResourceInvalidException
import org.json4s.Formats
import org.json4s.native.Serialization.read

import scala.util.{Failure, Try}

/**
 * Created by matt on 12/03/15.
 */
object Importer {

  /** Import list of whatever from a file, by deserializing the contents. */
  def importList[T](file: String)(implicit formats: Formats, manifest: Manifest[T]): Try[List[T]] = {

    try {
      val json = io.Source.fromInputStream(getClass.getResourceAsStream(file)).mkString
      readList(json)
    } catch {
      case x: NullPointerException => Failure(ResourceInvalidException(s"Could not locate file: '$file.'", x))
    }
  }

  def readList[T](json: String)(implicit formats: Formats, manifest: Manifest[T]): Try[List[T]] = {
    try {
      Try(read[List[T]](json))
    } catch {
      case e: Throwable => Failure(e)
    }
  }
}
