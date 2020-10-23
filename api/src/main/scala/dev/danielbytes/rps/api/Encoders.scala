package dev.danielbytes.rps.api

import java.time.{ Instant, LocalDateTime, ZoneId }

import io.circe.Decoder.Result
import io.circe.{ Decoder, Encoder, HCursor, Json }
import shapeless.Unwrapped

trait Encoders {

  implicit val TimestampFormat: Encoder[LocalDateTime] with Decoder[LocalDateTime] =
    new Encoder[LocalDateTime] with Decoder[LocalDateTime] {

      override def apply(a: LocalDateTime): Json =
        Encoder.encodeLong.apply(a.atZone(ZoneId.systemDefault()).toEpochSecond)

      override def apply(c: HCursor): Result[LocalDateTime] =
        Decoder.decodeLong.map(s => LocalDateTime.ofInstant(Instant.ofEpochMilli(s), ZoneId.systemDefault())).apply(c)
    }

  // see https://github.com/circe/circe/issues/297
  implicit def enumEncoder[E <: Enumeration](enum: E) =
    new Encoder[E#Value] {
      override def apply(a: E#Value): Json = Encoder.encodeString.apply(a.toString)
    }

  implicit def enumDecoder[E <: Enumeration](enum: E) =
    new Decoder[E#Value] {
      override def apply(c: HCursor): Result[E#Value] = Decoder.decodeString.map(str => enum.withName(str)).apply(c)
    }

  // see https://www.programcreek.com/scala/io.circe.Decoder
  implicit def anyValEncoder[V, U](implicit
    ev: V <:< AnyVal,
    V: Unwrapped.Aux[V, U],
    encoder: Encoder[U]): Encoder[V] = {
    val _ = ev
    encoder.contramap(V.unwrap)
  }

  implicit def anyValDecoder[V, U](implicit
    ev: V <:< AnyVal,
    V: Unwrapped.Aux[V, U],
    decoder: Decoder[U]): Decoder[V] = {
    val _ = ev
    decoder.map(V.wrap)
  }
}
