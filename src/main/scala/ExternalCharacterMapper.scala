
package com.github.rubyu.ebquery

import java.io.{FileInputStream, InputStream}
import scala.io
import util.control.Exception._

class ExternalCharacterMapper(stream: InputStream, charset: String = "MS932") {

  def this(path: String, charset: String) = this(new FileInputStream(path), charset)
  def this(path: String) = this(new FileInputStream(path))
  def this() = this(new EmptyInputStream)

  trait MappingValue
  case object NullValue extends MappingValue
  case object SkippedValue extends MappingValue
  case class StringValue(value: String) extends MappingValue

  val map: Map[String, MappingValue] = loadEBMapFile(stream, charset)

  private def hexString2unicode(s: String) = Integer.parseInt(s, 16).toChar

  def parseMappingValue(str: String) =
    allCatch opt {
      str.split(",")
        .map(s => hexString2unicode(s drop 1))
        .mkString
    } getOrElse ""

  def loadEBMapFile(stream: InputStream, charset: String) = {
    io.Source.fromInputStream(stream, charset)
      .getLines()
      .map { _
        .takeWhile(_ != '#')
        .split("\t")
        .toList
      }
      .withFilter(_.size >= 2)
      .collect {
        case x :: y :: _ => x.toLowerCase :: y :: parseMappingValue(y) :: Nil
      }
      .collect {
        case x :: "null" :: _ => (x, NullValue)
        case x :: "-" :: _    => (x, SkippedValue)
        case x :: y :: z :: Nil if z.nonEmpty => (x, StringValue(z))
      }
      .toMap
  }

  def getFullOption(code: Int): Option[MappingValue] =
    map.get("z" + code.toHexString.toLowerCase)

  def getHalfOption(code: Int): Option[MappingValue] =
    map.get("h" + code.toHexString.toLowerCase)
}
