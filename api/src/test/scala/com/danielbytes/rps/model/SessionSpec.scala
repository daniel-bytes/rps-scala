package com.danielbytes.rps.model

import org.scalatest._
import Inside._

import scala.util.Success

class SessionSpec
    extends WordSpec
    with Matchers {
  "Session" should {
    "handle serialization and deserialization to Map" in {
      val session = Session(
        SessionId("sessionid"),
        UserId("userid"),
        UserName("name"),
        UserSource.withName("Google")
      )

      inside(Session(session.toMap)) {
        case Success(result) => result shouldEqual session
      }
    }
  }
}
