package com.consideredgames.game.model.game

import com.consideredgames.game.logic.deployment.DeploymentProcessor
import com.consideredgames.game.logic.principal.PrincipalActionsProcessor

/**
 * Created by matt on 20/09/15.
 */
case class GameProcessors(deployment: DeploymentProcessor, principal: PrincipalActionsProcessor)
