package io.aakre

import java.net.URL
import scala.language.implicitConversions
import scala.util.matching.Regex

package object tiny_web_crawler {
  implicit def JavaURLtoString(url: URL): String = url.toString

  val UrlPattern: Regex = """<a href=["']([^"']+)["']""".r
}
