package io.aakre.tiny_web_crawler

import zio._
import zio.Console._

import java.net.URL

object Crawler extends zio.ZIOAppDefault {
  private val SampleUrl = "https://monzo.com/"
  private val crawlerParallelism = 4

  def run =
    for {
      _    <- printLine("Tiny web crawler")
      _    <- print(s"URL [$SampleUrl]:")
      url  <- getUrl
      _    <- printLine(s"Crawling $url...")
      res    <- crawl(url)
      _   <- printLine(res)
    } yield ()

  private def getUrl =
    readLine.map(s => if(s.isBlank) SampleUrl else s).map(s => if(s.endsWith("/")) s else s"$s/")
      .mapAttempt(s => new URL(s)).catchAll {
      _ => printLine(s"Error, invalid URL, using fallback: $SampleUrl") *> ZIO.succeed(new URL(SampleUrl))
    }

  private def crawl(startUrl: URL) =
    for {
      body  <- readPage(startUrl)
      links <- extractLinks(body, startUrl)
    } yield (startUrl, links.flatten)

  private def extractLinks(html: String, baseUrl: URL) =
    ZIO.attempt(UrlPattern.findAllIn(html).toList.map(filterAndCreateURLs(_, baseUrl)))

  private def filterAndCreateURLs(s: String, baseUrl: URL): Option[URL] = {
    val trimmed = if(s.startsWith("<a href=")) s.substring(9).dropRight(1) else s.dropRight(1)

    if(trimmed.isBlank || trimmed == "/" || trimmed == "#") None
    else {
      if(trimmed.startsWith(baseUrl)) Some(new URL(trimmed))
      else Some(new URL(baseUrl + (if(trimmed.startsWith("/")) trimmed.drop(1) else trimmed)))
    }
  }

  private def readPage(url: URL) =
    ZIO.attempt {
      val src = scala.io.Source.fromURL(url)(scala.io.Codec.UTF8)
      val res = src.mkString
      src.close()
      res
    }.catchAll(err => printLine(s"ERROR: unable to read $url") *> ZIO.fail(err))

}
