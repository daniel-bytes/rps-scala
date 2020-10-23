package dev.danielbytes.rps.model

import org.scalatest.Inside._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Success

class SessionSpec extends AnyWordSpec with TypeCheckedTripleEquals {
  "Session" should {
    "handle serialization and deserialization to Map" in {
      val session = Session(SessionId("sessionid"), UserId("userid"), UserName("name"), UserSource.withName("Google"))

      inside(Session(session.toMap)) {
        case Success(result) => assert(result === session)
      }
    }
  }
}
