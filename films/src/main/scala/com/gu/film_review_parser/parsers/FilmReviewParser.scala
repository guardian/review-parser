package com.gu.film_review_parser.parsers

import java.time.OffsetDateTime

import com.gu.contentapi.client.model.v1.Content
import com.gu.film_review_parser.ParsedFilmReview
import com.gu.film_review_parser.omdb.OMDB
import org.jsoup.Jsoup

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
      reviewSnippet <- fields.standfirst.map(s => Jsoup.parse(s).text())
      body <- fields.body
      omdbData <- OMDB.getData(title)
    } yield {
      ParsedFilmReview(content.id, internalComposerCode, creationDate, publicationDate, reviewer, starRating, reviewSnippet, title, omdbData.genre, omdbData.year, omdbData.imdbId, omdbData.directors, omdbData.actors)
    }

    parsed match {
      case Some(p) => println(s"Parsed ${content.id} as $parsed")
      case None => println(s"Failed to parse ${content.id}")
    }
    parsed
  }

  val newerTitlePattern = """^(.*) review ?(–|-|:).*""".r   //since Feb 2014
  val olderTitlePattern = """^(.*) (–|-|:) review.*""".r
  private def getTitle(text: String): Option[String] = {
    text match {
      case newerTitlePattern(title,_) => Some(title.trim)
      case olderTitlePattern(title,_) => Some(title.trim)
      case _ => None
    }
  }
}
