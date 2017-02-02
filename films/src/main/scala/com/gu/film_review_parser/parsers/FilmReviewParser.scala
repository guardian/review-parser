package com.gu.film_review_parser.parsers

import java.time.OffsetDateTime
import com.gu.contentapi.client.model.v1.Content
import com.gu.film_review_parser.ParsedFilmReview
import com.gu.film_review_parser.omdb.OMDB
import org.jsoup.Jsoup
import utils.ImageTransformer

object FilmReviewParser {
  def parseContent(content: Content): Option[ParsedFilmReview] = {
    val parsed = for {
      title <- getTitle(content.webTitle)
      fields <- content.fields
      internalComposerCode <- fields.internalComposerCode
      creationDate = fields.creationDate.map(time => OffsetDateTime.parse(time.iso8601))
      publicationDate = content.webPublicationDate.map(time => OffsetDateTime.parse(time.iso8601)).getOrElse(OffsetDateTime.now)
      reviewer <- fields.byline
      starRating <- fields.starRating
      reviewSnippet <- fields.standfirst.flatMap(s => getReviewSnippet(Jsoup.parse(s).text()))
      body <- fields.body
      omdbData <- OMDB.getData(title)
    } yield {
      val images = ImageTransformer.toAtomImages(content.elements.getOrElse(Nil))
      ParsedFilmReview(content.id, internalComposerCode, creationDate, publicationDate, reviewer, starRating, reviewSnippet, title, omdbData.genre, omdbData.year, omdbData.imdbId, omdbData.directors, omdbData.actors, images)
    }

    parsed match {
      case Some(p) => println(s"Parsed ${content.id} as $parsed")
      case None => println(s"Failed to parse ${content.id}")
    }
    parsed
  }

  /**
    * Titanic review - astonishing
    * Titanic review: astonishing
    */
  val titlePattern1 = """^((?!.*Film).*) review ?(–|-|:).*""".r

  /**
    * Titanic - review
    * Titanic: review
    */
  val titlePattern2 = """^(.*) ?(–|-|:) review.*""".r

  /**
    * Film review: Titanic
    */
  val titlePattern3 = """^Film review: (.*)""".r

  def getTitle(text: String): Option[String] = {
    text match {
      case titlePattern1(title,_) => Some(title.trim)
      case titlePattern2(title,_) => Some(title.trim)
      case titlePattern3(title) => Some(title.trim)
      case _ => None
    }
  }

  //Older reviews only have the certificate in the standfirst - exclude them
  val certificatePattern = """\([cC]ert""".r
  def getReviewSnippet(standfirst: String): Option[String] = {
    if (certificatePattern.findFirstIn(standfirst).isEmpty) Some(standfirst)
    else None
  }
}
