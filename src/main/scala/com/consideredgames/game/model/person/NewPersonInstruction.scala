package com.consideredgames.game.model.person

import com.consideredgames.game.model.deployment.Location
import com.consideredgames.game.model.player.PlayerColours.PlayerColour

/**
 * Created by matt on 10/03/15.
 */
case class NewPersonInstruction(id: Int, playerColour: PlayerColour, location: Option[Location] = None)