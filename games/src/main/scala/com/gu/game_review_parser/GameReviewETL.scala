package com.gu.game_review_parser

import com.gu.contentapi.client.GuardianContentClient
import com.gu.contentapi.client.model.{ItemQuery, SearchQuery}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object GameReviewETL extends App {

  if (args.isEmpty) {
    Console.err.println("Usage: <capi key> [<content id>]")
    Console.err.println("If a content id is not supplied, searches for all game reviews")
    sys.exit(1)
  }

  val capiKey: String = args(0)
  val itemId: Option[String] = args.lift(1)

  val capiClient = new GuardianContentClient(capiKey)

  val tags = "tone/reviews,technology/games"
  val showFields = "main,body,byline,creationDate,standfirst,starRating"

  itemId match {
    case Some(id) =>
      val query = ItemQuery(id)
        .tag(tags)
        .showFields(showFields)

      GameReviewProcessor.processItemQuery(capiClient, query)

    case None =>
      val query = SearchQuery()
        .tag(tags)
        .showFields(showFields)

      val firstPage = Await.result(capiClient.getResponse(query), 5.seconds)
      val pages = 1 to firstPage.pages
      GameReviewProcessor.processSearchQuery(pages, capiClient, query)
  }
}
