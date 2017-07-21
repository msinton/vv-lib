package com.consideredgames.serializers

import org.json4s.JsonAST.{JField, JObject, JString}
import org.json4s._
import org.json4s.reflect.TypeInfo

import scala.language.existentials
import scala.reflect.ClassTag

/**
 * Created by matt.sinton-hewitt on 23/02/2015.
 * Serializes a known set of objects using toString
 */
class AnySetSerializer[N <: Any : ClassTag](set: Set[_ <: Any], key: Option[String] = None) extends Serializer[Any] {

  def this(set: Set[_ <: Any], key: String) = this(set, Some(key))

  // first get works for a set of objects extending a class
  // second get otherwise
  private def getParentClass(r: Any) = Option(r.getClass.getDeclaringClass).getOrElse(r.getClass)

  val Class = classOf[Any]
  val setKey: String = key.getOrElse(getParentClass(set.head).getName) // different keys for different collections of objects

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Any] = {
    case (TypeInfo(clzz, _), JObject(List(JField(str, JString(n))))) if (Class isAssignableFrom clzz) && str == setKey =>
      set.find { x => x.toString == n}.getOrElse(throw new MappingException("Can't convert " + n + " to a " + setKey))
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case x: Any if getParentClass(x) == getParentClass(set.head) => JObject(List(JField(setKey, JString(x.toString))))
  }
}