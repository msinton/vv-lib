package com.consideredgames.game.model.game

import com.consideredgames.game.logic.deployment.DeploymentController
import com.consideredgames.game.logic.principal.{PrincipalEndController, PrincipalActionsController}

/**
 * Created by matt on 08/10/15.
 */
case class GameControllers(deployment: DeploymentController,
                           principal: PrincipalActionsController,
                           principalEnd: PrincipalEndController)
