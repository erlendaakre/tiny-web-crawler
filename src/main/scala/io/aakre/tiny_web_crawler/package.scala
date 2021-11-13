package io.aakre

import java.net.URL
import scala.language.implicitConversions

package object tiny_web_crawler {
  implicit def JavaURLtoString(url: URL) = url.toString
}
