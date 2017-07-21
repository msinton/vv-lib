package com.consideredgames.game.model.resources

/**
 * Created by matt on 09/03/15.
 */
trait Mergeable[T] {

  def n: Long

  def merge(mergeable: T): Option[T]

  def canMerge(mergeable: T): Boolean
}
