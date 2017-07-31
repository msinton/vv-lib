package com.consideredgames.game.state

/**
  * Created by matt on 18/07/17.
  */
trait Error
case class Errors(errors: List[Error])

trait OneReasonError extends Error {
  def reason: String
}

trait ReasonsError extends Error {
  def reasons: List[String]
}

case class StartGameError(reason: String) extends OneReasonError

object Errors {
  def apply(error: Error, errors: List[Error]): Errors = new Errors(error :: errors)
}