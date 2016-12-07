package com.gu.film_review_parser

import com.gu.contentapi.client.GuardianContentClient
import com.gu.contentapi.client.model.{ItemQuery, SearchQuery}
import com.gu.film_review_parser.parsers.FilmReviewParser

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

object FilmReviewProcessor {

  //failed items will be taken down
  case class ParsedResults(successful: Seq[ParsedFilmReview], failed: Seq[ParsedFilmReview])

  def processSearchQuery(page: Int, capiClient: GuardianContentClient, query: SearchQuery): ParsedResults = {
    Thread.sleep(500)

    Try(Await.result(capiClient.getResponse(query.page(page)), 5.seconds)) match {
      case Success(response) =>
        response.results.foldLeft(ParsedResults(Nil,Nil)) { (results, content) =>
          val newResults = FilmReviewParser.parseContent(content).map(parsed => results.copy(successful = results.successful :+ parsed))
            .orElse(ParsedFilmReview.reviewForTakedown(content).map(parsed => results.copy(failed = results.failed :+ parsed)))

          newResults.getOrElse(results)
        }
      case Failure(e) =>
        println(s"Skipping page $page because of CAPI failure (${e.getMessage})")
        ParsedResults(Nil,Nil)
    }
  }

  def processItemQuery(capiClient: GuardianContentClient, query: ItemQuery): ParsedResults = {
    Try(Await.result(capiClient.getResponse(query), 5.seconds)) match {
      case Success(response) =>
        response.content.flatMap { content =>
          FilmReviewParser.parseContent(content).map(parsed => ParsedResults(Seq(parsed), Nil))
            .orElse(ParsedFilmReview.reviewForTakedown(content).map(parsed => ParsedResults(Nil, Seq(parsed))))
        }.getOrElse(ParsedResults(Nil,Nil))

      case Failure(e) =>
        println(s"Skipping id ${query.id} because of CAPI failure (${e.getMessage})")
        ParsedResults(Nil,Nil)
    }
  }
}
