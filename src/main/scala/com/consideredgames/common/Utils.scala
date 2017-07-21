package com.consideredgames.common

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.Random

/**
 * Created by matt on 17/03/15.
 */
object Utils {

  type ToProperties[A, B] = A => Traversable[B]

  /**
   * For items in a list which have some property that can be viewed as a list, this creates an inverse mapping from
   * those properties to the items.
   *
   * e.g.
   * You have a list of strings and you want a mapping of characters to the strings that contain said character.
   * createInverseMapping(list)(s.toCharArray)
   *
   * @param list The list of items to create the mapping from
   * @param f from item to properties that will be the keys in the map
   * @param g from item to desired value that represents that item
   * @tparam Y the type of the item
   * @tparam X the type of the properties
   * @tparam Z the type of the values
   * @return An inverse mapping from the properties to the items as a Set
   */
  def createInverseMappingWithValueTransform[Y, X <: AnyRef, Z](list: Seq[Y])(g: Y => Z)(f: ToProperties[Y, X]): (collection.Map[X, Set[Z]]) = {

    val map = mutable.AnyRefMap.empty[X, Set[Z]]

    for (item <- list) {
      val xs = f(item)
      for (x <- xs) {
        map.update(x, map.getOrElse(x, Set()) + g(item))
      }
    }
    map
  }

  /**
   * For items in a list which have some property that can be viewed as a list, this creates an inverse mapping from
   * those properties to the items.
   *
   * e.g.
   * You have a list of strings and you want a mapping of characters to the strings that contain said character.
   * createInverseMapping(list)(s.toCharArray)
   *
   *
   * @param list The list of items to create the mapping from
   * @param f from item to properties that will be the keys in the map
   * @tparam X the type of the properties
   * @tparam Z the type of the item
   * @return An inverse mapping from the properties to the items as a Set
   */
  def createInverseMapping[X <: AnyRef, Z](list: Seq[Z])(f: ToProperties[Z, X]): (collection.Map[X, Set[Z]]) = {
    createInverseMappingWithValueTransform[Z, X, Z](list)({ z: Z => z})(f)
  }

  /**
   * removes an element randomly from the buffer and returns it.
   *
   * @tparam T the type contained by the buffer
   * @return Some(element) removed. None if the list was empty
   */
  def randomRemove[T](list: mutable.Buffer[T], random: Random): Option[T] = {
    if (list.nonEmpty) {
      val e = list.remove(random.nextInt(list.size))
      Option(e)
    }
    else
      None
  }

  def getRandomWeighted[T](list: List[T], weightingsTotal: Int, weighting: T => Int, random: Random): T = {
    @tailrec
    def getElement(n: Int, acc: Int, x: T, xs: List[T]): T = {
      if (n < acc || xs.isEmpty) {
        x
      } else {
        getElement(n, acc + weighting(x), xs.head, xs.tail)
      }
    }
    getElement(random.nextInt(weightingsTotal), 0, list.head, list.tail)
  }

  def getRandomWeighted[T <: HasWeighting](list: List[T], weightingsTotal: Int, random: Random): T =
    getRandomWeighted(list, weightingsTotal, {t: T => t.weighting}, random)

  def getRandom[T](list: Iterable[T], random: Random): T = {
    val index = random.nextInt(list.size)
    list.toBuffer(index)
  }
}