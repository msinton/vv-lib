package com.consideredgames.game.model.game

import com.consideredgames.serializers.{Named, NamedSetSerializer}

/**
 * Created by matt on 08/10/15.
 */
object Phases {

  sealed abstract class Phase(val name: String) extends Named

  case object Deployment extends Phase(name = "deployment")
  case object Principal extends Phase(name = "principal")
  case object PrincipalEnd extends Phase(name = "principal end")
  case object Movement extends Phase(name = "movement")

  val phases = List(Deployment, Principal, PrincipalEnd, Movement)

  val phasesSet = phases.toSet

  val serializer = new NamedSetSerializer[Phase](phasesSet, Some("phase"))

}