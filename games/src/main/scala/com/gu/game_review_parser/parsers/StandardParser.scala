package com.gu.game_review_parser.parsers

import java.time.OffsetDateTime

import com.gu.contentapi.client.model.v1.Content
import com.gu.game_review_parser.ParsedGameReview
import org.jsoup.Jsoup
import utils.ImageTransformer

import scala.util.Try

object StandardParser {
  def parseContent(content: Content): Option[ParsedGameReview] = {
    val parsed = for {
      title <- getTitle(content.webTitle)
      fields <- content.fields
      internalComposerCode <- fields.internalComposerCode
      creationDate = fields.creationDate.map(time => OffsetDateTime.parse(time.iso8601))
      publicationDate = content.webPublicationDate.map(time => OffsetDateTime.parse(time.iso8601)).getOrElse(OffsetDateTime.now)
      reviewer <- fields.byline
      starRating <- fields.starRating
      reviewSnippet <- fields.standfirst.map(s => Jsoup.parse(s).text())
      body <- fields.body
      details <- getDetails(body)
    } yield {
      val images = ImageTransformer.toAtomImages(content.elements.getOrElse(Nil))
      ParsedGameReview(content.id, internalComposerCode, creationDate, publicationDate, reviewer, starRating, reviewSnippet, title, Some(details.publisher), details.platforms, Some(details.price), details.pegiRating, genre = Nil, images = images)
    }

    parsed match {
      case Some(p) => println(s"Parsed ${content.id} as $parsed")
      case None => println(s"Failed to parse ${content.id}")
    }
    parsed
  }

  private case class Details(publisher: String,
                     platforms: List[String],
                     price: String,
                     pegiRating: Option[Int])

  private def getDetails(body: String): Option[Details] = {
    val doc = Jsoup.parse(body)
    Option(doc.select("p > strong").first) flatMap  { details =>

      val detailsString = details.html
      val tokens = detailsString.split(";")

      /**
        * We're looking for a string with the following format:
        * "Publisher; platforms; price; pegiRating"
        *
        * Where:
        * - Publisher is just a string
        *
        * - Platforms is a list delimited by either:
        *   - "PC / PS4 / XBox One", or
        *   - "PC, PS4 and XBox One"
        *   One of these may be followed by the text "(version tested)"
        *
        * - Price is in £s
        *
        * - PegiRating begins "Pegi rating:" and may end with a "+"
        */
      for {
        publisher <- tokens.headOption
        platforms = tokens.lift(1).map(platformsString => PlatformParser.getPlatforms(platformsString)).getOrElse(Nil)
        price <- tokens.lift(2).flatMap(getPrice)
        pegiRating = tokens.lift(3).flatMap(getPegiRating)
      } yield Details(publisher, platforms, price, pegiRating)
    }
  }

  val titlePattern = """^(.*) review ?(–|-|:).*""".r
  private def getTitle(text: String): Option[String] = {
    text match {
      case titlePattern(title,_) => Some(title.trim)
      case _ => None
    }
  }

  private def getPrice(text: String): Option[String] = {
    val trimmed = text.trim
    if (trimmed.matches("""^£\d+([,.]\d\d)?$""")) Some(trimmed)
    else None
  }

  val pegiRatingPattern = """pegi (rating: )?(\d{1,2})\+?""".r
  private def getPegiRating(text: String): Option[Int] = {
    text.trim.toLowerCase match {
      case pegiRatingPattern(_, age) => Try(age.toInt).toOption
      case _ => None
    }
  }

  object PlatformParser {
    def getPlatforms(text: String): List[String] = {
      val tokens = text.split(",|and|/")
      tokens.toList.flatMap(getPlatform)
    }

    private def getPlatform(text: String): Option[String] = {
      val processed = {
        val versionTestedIdx = text.indexOf("(version tested)")
        if (versionTestedIdx > -1) text.substring(0, versionTestedIdx)
        else text
      }
      val maybePlatform = ParsedGameReview.platforms.get(processed.toLowerCase.trim)
      if (maybePlatform.isEmpty) println(s"Unknown platform string: $text")
      maybePlatform
    }
  }
}
