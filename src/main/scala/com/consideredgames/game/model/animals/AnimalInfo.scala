package com.consideredgames.game.model.animals

import com.consideredgames.common.Importer
import com.consideredgames.game.model.resources.Resources
import com.consideredgames.serializers.Named
import org.json4s.DefaultFormats

import scala.util.Try

/**
 * Created by matt on 27/02/15.
 */
case class AnimalInfo(name: String, pregnancy: Pregnancy, harvest: Harvest, hexCapacity: Int, personCapacity: Int, rarity: Int) extends Named

object AnimalInfo {

  val serializer = Resources.serializer

  implicit val formats = DefaultFormats + serializer

  /** Import Animals from a file */
  def importFromFile(animalsFile: String = "/animals.json"): Try[List[AnimalInfo]] = Importer.importList[AnimalInfo](animalsFile)

  def readFromString(json: String): Try[List[AnimalInfo]] = Importer.readList[AnimalInfo](json)
}