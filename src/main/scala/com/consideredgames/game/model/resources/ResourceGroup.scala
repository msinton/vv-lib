package com.consideredgames.game.model.resources

/**
 * Created by matt on 09/03/15.
 */
case class ResourceGroup (r: Resources.Resource, n: Long) extends Mergeable[ResourceGroup] {

  override def merge(mergeable: ResourceGroup): Option[ResourceGroup] = {
    canMerge(mergeable) match {
      case true => Some(ResourceGroup(r, n + mergeable.n))
      case _ => None
    }
  }

  override def canMerge(mergeable: ResourceGroup): Boolean = mergeable.r == r
}
