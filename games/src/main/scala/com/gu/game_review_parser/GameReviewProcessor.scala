package com.gu.game_review_parser

import com.gu.contentapi.client.GuardianContentClient
import com.gu.contentapi.client.model.v1.Content
import com.gu.contentapi.client.model.{ItemQuery, SearchQuery}
import com.gu.game_review_parser.parsers.{ObserverParser, StandardParser}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

object GameReviewProcessor {
  def processSearchQuery(pages: Seq[Int], capiClient: GuardianContentClient, query: SearchQuery): Seq[ParsedGameReview] = {
    pages.flatMap { page =>
      Thread.sleep(500)

      Try(Await.result(capiClient.getResponse(query.page(page)), 5.seconds)) match {
        case Success(response) => response.results.flatMap(processContent)
        case Failure(e) =>
          println(s"Skipping page $page because of CAPI failure (${e.getMessage})")
          Nil
      }
    }
  }

  def processItemQuery(capiClient: GuardianContentClient, query: ItemQuery): Seq[ParsedGameReview] = {
    Try(Await.result(capiClient.getResponse(query), 5.seconds)) match {
      case Success(response) => response.content.map(processContent) getOrElse Nil
      case Failure(e) =>
        println(s"Skipping id ${query.id} because of CAPI failure (${e.getMessage})")
        Nil
    }
  }

  def processContent(content: Content): Seq[ParsedGameReview] = {
    content.fields.flatMap(_.publication).map {
      case "The Observer" => ObserverParser.parseContent(content)
      case _ => List(StandardParser.parseContent(content)).flatten
    } getOrElse Nil
  }
}
