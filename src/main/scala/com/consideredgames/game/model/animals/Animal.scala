package com.consideredgames.game.model.animals

import com.consideredgames.game.model.exceptions.ResourceInvalidException
import org.json4s.JsonAST.{JField, JObject, JString}
import org.json4s._

/**
 * Created by matt on 09/03/15.
 */
case class Animal (info: AnimalInfo, female: Boolean, var wild: Boolean, var pregnant: Option[Pregnant]) {

  def isPregnant = pregnant.isDefined
}

object Animal {

  def apply(info: AnimalInfo, female: Boolean, wild: Boolean, pregnant: Pregnant): Animal = {
    apply(info, female, wild, Some(pregnant))
  }

  def apply(info: AnimalInfo, female: Boolean, pregnant: Pregnant): Animal = {
    apply(info, female, wild = true, Some(pregnant))
  }

  def apply(info: AnimalInfo, female: Boolean, wild: Boolean): Animal = {
    apply(info, female, wild = true, None)
  }

  def apply(info: AnimalInfo, female: Boolean): Animal = {
    apply(info, female, wild = true)
  }
  //using a custom serializer that shortens AnimalInfo to its name

  def serializer(animalInfos: List[AnimalInfo]): CustomSerializer[Animal] = {

    val key = "Animal"

    object serializer extends CustomSerializer[Animal](
      format => ( {
        case JObject(List(JField(str, JString(n)))) if str == key =>
          val splits = n.split(',')
          if (splits.length == 3) {
            Animal(animalInfos.find(_.name == splits(0)).getOrElse(throw ResourceInvalidException(s"Could not deserialize animal, as not an animal Info match $n")),
              splits(1).toBoolean, splits(2).toBoolean)
          } else {
            Animal(animalInfos.find(_.name == splits(0)).getOrElse(throw ResourceInvalidException(s"Could not deserialize animal, as not an animal Info match $n")),
              splits(1).toBoolean,  splits(2).toBoolean, Option(Pregnant(splits(3).toInt)))
          }
      }, {
        case x: Animal =>
          val info = x.info.name
          val w = x.wild
          val f = x.female
          var str = s"$info,$f,$w"
          x.pregnant.foreach { p => str = str + "," + p.term}
          JObject(List(JField(key, JString(str))))
      }
        )
    )
    serializer
  }

}

