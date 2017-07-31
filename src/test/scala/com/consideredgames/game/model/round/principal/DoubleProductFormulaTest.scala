package com.consideredgames.game.model.round.principal

import com.consideredgames.game.model.resources.Resources.{GoodMeal, Meal}
import com.consideredgames.game.model.resources.{ResourceGroup, ResourceProduct}
import org.scalatest.FunSuite

/**
 * Created by matt on 16/04/15.
 */
class DoubleProductFormulaTest extends FunSuite {

  test("works") {
    val formula = DoubleProductFormula(Meal, GoodMeal, List(1,2,3,4,5), List(3,5,7,9,11))

    assert(formula.produce == List(ResourceProduct(List(ResourceGroup(Meal,1), ResourceGroup(GoodMeal,3))),
      ResourceProduct(List(ResourceGroup(Meal,2), ResourceGroup(GoodMeal,5))),
      ResourceProduct(List(ResourceGroup(Meal,3), ResourceGroup(GoodMeal,7))),
      ResourceProduct(List(ResourceGroup(Meal,4), ResourceGroup(GoodMeal,9))),
      ResourceProduct(List(ResourceGroup(Meal,5), ResourceGroup(GoodMeal,11)))))
  }

  test("first list shorter cuts off end of other list") {
    val formula = DoubleProductFormula(Meal, GoodMeal, List(1,2,3), List(3,5,7,9,11))

    assert(formula.produce == List(ResourceProduct(List(ResourceGroup(Meal,1),
      ResourceGroup(GoodMeal,3))), ResourceProduct(List(ResourceGroup(Meal,2), ResourceGroup(GoodMeal,5))),
      ResourceProduct(List(ResourceGroup(Meal,3), ResourceGroup(GoodMeal,7)))))
  }

  test("second list shorter results in zero values for missing levels") {
    val formula = DoubleProductFormula(Meal, GoodMeal, List(1,2,3), List(3))

    assert(formula.produce == List(ResourceProduct(List(ResourceGroup(Meal,1), ResourceGroup(GoodMeal,3))),
      ResourceProduct(List(ResourceGroup(Meal,2), ResourceGroup(GoodMeal,0))),
      ResourceProduct(List(ResourceGroup(Meal,3), ResourceGroup(GoodMeal,0)))))
  }
}
