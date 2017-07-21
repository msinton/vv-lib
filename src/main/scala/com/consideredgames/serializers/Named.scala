package com.consideredgames.serializers

/**
 * Created by matt on 27/02/15.
 */
trait Named {
  def name: String
}

object Named {
  implicit def orderingByName[Y <: Named]: Ordering[Y] = Ordering.by(_.name)
}