package com.consideredgames.game.model.animals

import com.consideredgames.game.model.resources.ResourceGroup

/**
 * Created by matt on 09/03/15.
 */
case class Harvest(requires: Int, resourcesGained: List[ResourceGroup], meatGained: Int)
