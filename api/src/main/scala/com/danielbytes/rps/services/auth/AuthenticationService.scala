package com.danielbytes.rps.services.auth

import com.danielbytes.rps.model.{ AuthenticationError, User }

import scala.concurrent.Future

/**
 * Authentication token model contract
 */
trait TokenRequest {
  def id: String
  def name: String
}

/**
 * Authentication service contract
 */
trait AuthenticationService {
  type Request <: TokenRequest
  type Response = Future[Either[AuthenticationError, User]]

  /**
   * Authenticates a token, yielding a User or error
   */
  def authenticate(token: Request): Response
}
