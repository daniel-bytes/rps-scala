package com.danielbytes.rps.model

import akka.http.scaladsl.model.{ StatusCode, StatusCodes }

/**
 * Base trait for all application logic errors
 */
sealed trait ApplicationError {
  def status: StatusCode = StatusCodes.InternalServerError
}

/**
 * Exception that holds an [[ApplicationError]]
 * @param errorType The error condition
 */
case class ApplicationErrorException(error: ApplicationError) extends Exception

/**
 * ApplicationError type that indicates a rule has been broken
 */
sealed trait RuleViolationError extends ApplicationError {
  override def status: StatusCode = StatusCodes.BadRequest
}

case object OtherPlayersTokenError extends RuleViolationError
case object NotATokenError extends RuleViolationError
case object NotAMovableTokenError extends RuleViolationError
case object CannotAttackYourOwnTokenError extends RuleViolationError
case object MoveIsTooFarError extends RuleViolationError
case object WrongPlayerTurnError extends RuleViolationError

sealed trait CombatRulesError extends RuleViolationError
case object TokenNotCapableOfAttackError extends CombatRulesError

sealed trait GameStateError extends RuleViolationError
case object NoFlags extends GameStateError
case object NoTokens extends GameStateError
case object NoMovableTokens extends GameStateError

sealed trait GameServiceError extends ApplicationError

case object GameNotFoundError extends GameServiceError {
  override def status: StatusCode = StatusCodes.NotFound
}

case object PlayerNotFoundError extends GameServiceError {
  override def status: StatusCode = StatusCodes.NotFound
}

sealed trait AuthenticationError extends ApplicationError
case class AuthenticationFailedError(msg: String) extends AuthenticationError

/**
 * An error that indicates the game board geometry is in an invalid and unfixable state
 */
case class BoardGeometryException(
  rows: Int,
  columns: Int
) extends IllegalStateException(s"Illegal game board geometry: $rows rows, $columns columns.")

/**
 * An error that indicates the game player is incorrect
 */
case class IncorrectPlayerException() extends IllegalStateException(s"Illegal player.")

/**
 * An error that indicates the game board token count is invalid
 */
case class BoardTokenCountException(
  rows: Int,
  columns: Int,
  flagCount: Int,
  bombCount: Int,
  rockCount: Int,
  paperCount: Int,
  scissorCount: Int
) extends IllegalStateException(
  s"Illegal token count for game board token count: $rows rows, $columns columns, " +
    s"$flagCount flags, $bombCount bombs, $rockCount rocks, $paperCount papers, $scissorCount scissors."
)