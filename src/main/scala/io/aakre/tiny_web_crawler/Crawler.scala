package main.scala.io.aakre.tiny_web_crawler
import zio.{ExitCode, URIO, ZIO}

object Crawler extends zio.App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    ZIO.succeed {
      print("TODO: stuff!")
    }.exitCode
  }
}
