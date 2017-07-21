package com.consideredgames.game.model.round.principal

import com.consideredgames.game.model.resources.Resources.Resource
import com.consideredgames.game.model.resources.{ResourceGroup, ResourceProduct}
import com.consideredgames.game.model.season.Seasons._

/**
 * Created by matt on 16/04/15.
 */
sealed trait Produces {
  def produce: List[_]
  def maxLevel: Int = produce.size - 1
}

trait ResourceProducts extends Produces {
  def produce: List[ResourceProduct]
}

trait ProducesByLevels extends Produces {
  def levels: List[Int]
}

case class LevelProducts(levels: List[Int]) extends Produces {
  def produce = levels
}

case class SimpleProductFormula(resource: Resource, levels: List[Int]) extends ResourceProducts with ProducesByLevels {
  val produce_ = levels map (level => ResourceProduct(List(ResourceGroup(resource, level.toLong))))

  override def produce: List[ResourceProduct] = produce_
}

case class DoubleProductFormula(resource1: Resource, resource2: Resource, levels1: List[Int], levels2: List[Int]) extends ResourceProducts {
  val produce1_ = levels1 map (level => ResourceProduct(List(ResourceGroup(resource1, level.toLong))))
  val produce2_ = levels2 map (level => ResourceProduct(List(ResourceGroup(resource2, level.toLong))))
  val produce_ = produce1_.zipWithIndex map { case (value, index) => ResourceProduct(value.product ++ {
    if (produce2_.size > index)
      produce2_(index).product
    else
      List(ResourceGroup(resource2, 0))
  })}

  override def produce: List[ResourceProduct] = produce_
}

case class DelegatingResourceProvider() extends ResourceProducts {
  var producer: ResourceProducts = _

  override def produce: List[ResourceProduct] = producer.produce
}

/**
 * The resource needs to be provided with setResource before calling produce.
 */
//case class ResourceProvidedProductFormula(levels: List[Int]) extends ResourceProducts with ProducesByLevels {
//  var produce_ : List[ResourceProduct] = _
//
//  def setResource(r: Resource) = {
//    produce_ = levels map { level => ResourceProduct(List(ResourceGroup(r, level.toLong)))}
//  }
//
//  override def produce: List[ResourceProduct] = produce_
//}

case class PersonProduction(percent: Int)

trait PersonProducts extends Produces {
  def produce: List[PersonProduction]
}

case class SimplePersonProductFormula(levels: List[Int]) extends PersonProducts {
  override def produce: List[PersonProduction] = levels map (l => PersonProduction(l))
}

/**
 * Can craft a number of tools as indicated by produce - indexed by skill number
 */
trait ToolProducts extends Produces {
  def produce: List[Int]
}

case class SimpleToolProductFormula(produce: List[Int]) extends ToolProducts

/**
 * @param base the base (level 1) chances of successfully hunting the respective numbers of animals, starting at 1
 */
case class AnimalProductFormula(maxLevel_ : Int, base: List[Int]) extends Produces {

  val prod = (1 to maxLevel_).toList map {case level => base.map( baseVal => math.max(0, math.min(baseVal + level*2, 100)))}

  def produce: List[List[Int]] = prod
}

case class WeatherResourceProduct(resource: Resource, spring: Int, summer: Int, autumn: Int, winter: Int) {
  def value(seasonType: SeasonType) = seasonType match {
    case Spring => spring
    case Summer => summer
    case Autumn => autumn
    case Winter => winter
  }
}

case class WeatherProductFormula(resource: Resource, spring: IndexedSeq[Int], summer: IndexedSeq[Int], autumn: IndexedSeq[Int], winter: IndexedSeq[Int]) extends Produces {

  var produce_ : List[WeatherResourceProduct] = List()
  val maxLength = math.min(math.min(math.min(spring.length, summer.length), autumn.length), winter.length)
  (0 until maxLength).foreach(x => produce_ = WeatherResourceProduct(resource, spring(x), summer(x), autumn(x), winter(x)) :: produce_)

  override def produce: List[WeatherResourceProduct] = produce_
}