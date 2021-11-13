package io.aakre.tiny_web_crawler

import zio._
import zio.Console._

import java.io.{BufferedWriter, File, PrintWriter}
import java.net.URL

object Crawler extends zio.ZIOAppDefault {
  private val SampleUrl = "https://monzo.com/"

  final case class CrawlerState(linksToCheck: Set[URL], result: Map[URL, Set[URL]]) { self =>
    def done: Boolean = linksToCheck.isEmpty
    def update(url: URL, links: Set[URL]): CrawlerState =
      self.copy(linksToCheck ++ (links removedAll result.keySet) - url, result + (url -> links))
  }
  object CrawlerState {
    def seed(url: URL): UIO[Ref[CrawlerState]] = Ref.make(CrawlerState(Set(url), Map.empty[URL, Set[URL]]))
  }

  def run =
    for {
      _      <- printLine("Tiny web crawler")
      _      <- print(s"URL [$SampleUrl]:")
      base   <- getStartUrl
      _      <- printLine(s"Crawling $base...")
      state  <- CrawlerState.seed(base)
      _      <- mainLoop(state, base).repeatUntilZIO(_ => state.get.map(s => s.done))
      result <- state.get
      _      <- printResults(result)
      _      <- persistResults(result)
    } yield result

  private def mainLoop(stateRef: Ref[CrawlerState], baseUrl: URL) =
    for {
      state      <- stateRef.get
      _          <- printLine(s"links checked: ${state.result.keySet.size}, remaining: ${state.linksToCheck.size}")
      url        = state.linksToCheck.head
      (_, links) <- crawl(url, baseUrl)
      _          <- stateRef.update(_.update(url, links))
    } yield ()


  private def getStartUrl =
    readLine.map(s => if (s.isBlank) SampleUrl else s).map(s => if (s.endsWith("/")) s else s"$s/")
      .mapAttempt(s => new URL(s)).catchAll {
      _ => printLine(s"Error, invalid URL, using fallback: $SampleUrl") *> ZIO.succeed(new URL(SampleUrl))
    }

  private def crawl(url: URL, base: URL) =
    for {
      body  <- readPage(url)
      links <- extractLinks(body, base)
    } yield (url, links.flatten)

  private def extractLinks(html: String, baseUrl: URL) =
    ZIO.attempt(UrlPattern.findAllIn(html).toSet.map(filterAndCreateURLs(_, baseUrl)))

  private def filterAndCreateURLs(s: String, baseUrl: URL): Option[URL] = {
    val trimmed = if (s.startsWith("<a href=")) s.substring(9).dropRight(1) else s.dropRight(1)

    if (trimmed.isBlank || trimmed == "/") None
    else {
      if (trimmed.startsWith(baseUrl)) Some(new URL(trimmed))
      else if (trimmed.startsWith("#") || trimmed.startsWith("http") || trimmed.startsWith("mailto:")
        || trimmed.startsWith("tel:") || trimmed.endsWith(".pdf") || trimmed.endsWith(".png")
        || trimmed.endsWith(".jpg")) None
      else Some(new URL(baseUrl + (if (trimmed.startsWith("/")) trimmed.drop(1) else trimmed)))
    }
  }

  private def readPage(url: URL) =
    ZIO.attempt {
      val src = scala.io.Source.fromURL(url)(scala.io.Codec.UTF8)
      val res = src.mkString
      src.close()
      res
    }.catchAll(err => printLine(s"ERROR: unable to read $url\n$err") *> ZIO.succeed(""))

  private def printResults(state: Crawler.CrawlerState) =
    for {
      _   <- printLine(s"Total urls crawled: ${state.result.keySet.size}")
      top = state.result.toList.sortWith(_._2.size > _._2.size).head
      _   <- printLine(s"Page with most links: ${top._1} (${top._2.size})")
    } yield ()

  private def persistResults(state: Crawler.CrawlerState) =
    ZIO.attempt {
      val f = new File("result.txt")
      println(s"Writing results to file: ${f.getAbsoluteFile.toString}")
      val w = new BufferedWriter(new PrintWriter(f))
      state.result.foreach { case (url, links) =>
        w.append(url + ⏎ + "\t" + links.mkString(",") + ⏎)
      }
      w.close()
    }
}
