package utils

import io.circe.{Decoder, Json}

import scala.io.Source._
import io.circe.parser._

object JsonHelpers {
  def loadFile(path: String): String = fromFile(path).mkString

  def loadJson(path: String): Json = parse(loadFile(path)).getOrElse(sys.error(s"Error parsing $path"))

  def decodeFromFile[T : Decoder](path: String): T = loadJson(path).as[T].getOrElse(sys.error(s"Error decoding $path"))
}
