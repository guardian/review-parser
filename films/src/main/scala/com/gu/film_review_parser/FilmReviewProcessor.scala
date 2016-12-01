package com.gu.film_review_parser

import com.gu.contentapi.client.GuardianContentClient
import com.gu.contentapi.client.model.{ItemQuery, SearchQuery}
import com.gu.film_review_parser.parsers.FilmReviewParser

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

object FilmReviewProcessor {

  def processSearchQuery(pages: Seq[Int], capiClient: GuardianContentClient, query: SearchQuery): Seq[ParsedFilmReview] = {
    pages.flatMap { page =>
      Thread.sleep(500)

      Try(Await.result(capiClient.getResponse(query.page(page)), 5.seconds)) match {
        case Success(response) => response.results.flatMap(FilmReviewParser.parseContent)
        case Failure(e) =>
          println(s"Skipping page $page because of CAPI failure (${e.getMessage})")
          Nil
      }
    }
  }

  def processItemQuery(capiClient: GuardianContentClient, query: ItemQuery): Option[ParsedFilmReview] = {
    Try(Await.result(capiClient.getResponse(query), 5.seconds)) match {
      case Success(response) => response.content.flatMap(FilmReviewParser.parseContent)
      case Failure(e) =>
        println(s"Skipping id ${query.id} because of CAPI failure (${e.getMessage})")
        None
    }
  }
}
