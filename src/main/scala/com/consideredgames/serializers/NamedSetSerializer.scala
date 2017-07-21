package com.consideredgames.serializers

import org.json4s.JsonAST.{JField, JObject, JString}
import org.json4s._

import scala.reflect.ClassTag

/**
 * Serializer for Named Case Objects declared within a single Object - for example Resources.
 * The (key, named.name) combinations must be unique.
 */
class NamedSetSerializer [N <: Named : ClassTag](named: Set[_ <: Named], key: Option[String] = None) extends Serializer[Named] {

  def this(named: Set[_ <: Named], key: String) = this(named, Option(key))

  private def getParentClass(r: Named) = r.getClass.getDeclaringClass

  val NamedClass = classOf[Named]
  val namedKey: String = key.getOrElse(getParentClass(named.head).getName) // different keys for different collections of objects

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Named] = {
    case (TypeInfo(clzz, _), JObject(List(JField(str, JString(n))))) if (NamedClass isAssignableFrom (clzz)) && str == namedKey =>
      named.find { x => x.name == n}.getOrElse(throw new MappingException(s"Can't convert $n to a $namedKey"))
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case x: Named if getParentClass(x) == getParentClass(named.head) => JObject(List(JField(namedKey, JString(x.name))))
  }
}
