package com.consideredgames.game.model.game

import com.consideredgames.game.logic.setup.ToolOrder
import com.consideredgames.game.model.animals.AnimalInfo
import com.consideredgames.game.model.person.tools.Tools
import com.consideredgames.game.model.player.Player

case class NewGameConfig(players: List[Player],
                         seed: Long,
                         animalInfos: List[AnimalInfo],
                         tools: Tools,
                         startingTools: List[ToolOrder])
