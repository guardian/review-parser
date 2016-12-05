package com.gu.film_review_parser

import java.time.OffsetDateTime

import com.gu.auxiliaryatom.model.auxiliaryatomevent.v1.{AuxiliaryAtom, AuxiliaryAtomEvent, EventType => AuxiliaryAtomEventType}
import com.gu.contentapi.client.model.{ItemQuery, SearchQuery}
import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import integration.{AtomPublisher, ReviewParserConfig}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object FilmReviewETL extends App {

  if (args.isEmpty) {
    Console.err.println("Usage: <CODE|PROD> [<content id>]")
    Console.err.println("If a content id is not supplied, searches for all film reviews")
    sys.exit(1)
  }

  val stage: String = args(0)
  val itemId: Option[String] = args.lift(1)

  val config = ReviewParserConfig(stage)

  val tags = "tone/reviews,film/film"
  val showFields = "main,body,byline,creationDate,standfirst,starRating,internalComposerCode"

  itemId match {
    case Some(id) =>
      val query = ItemQuery(id)
        .tag(tags)
        .showFields(showFields)

      FilmReviewProcessor.processItemQuery(config.capiConfig.capiClient, query) match {
        case Some(parsed) => sendAtoms(Seq(parsed))
        case None => println(s"Failed to parse $id")
      }

    case None =>
      val query = SearchQuery()
        .tag(tags)
        .showFields(showFields)

      val firstPage = Await.result(config.capiConfig.capiClient.getResponse(query), 5.seconds)
      val pages = 1 to firstPage.pages
      val parsed = FilmReviewProcessor.processSearchQuery(pages, config.capiConfig.capiClient, query)

      println(s"Successfully parsed ${parsed.size} film reviews.")

      sendAtoms(parsed)
  }

  config.capiConfig.capiClient.shutdown()

  private def sendAtoms(parsed: Seq[ParsedFilmReview]): Unit = {
    val atomEvents: Seq[(AuxiliaryAtomEvent, ContentAtomEvent)] = parsed map { review =>
      val contentAtom = ParsedFilmReview.toAtom(review)
      val auxiliaryAtomEvent = AuxiliaryAtomEvent(review.internalComposerCode, eventType = AuxiliaryAtomEventType.Add, Seq(AuxiliaryAtom(contentAtom.id, "review")))
      val contentAtomEvent = ContentAtomEvent(contentAtom, EventType.Update, eventCreationTime = review.creationDate.getOrElse(OffsetDateTime.now).toInstant.toEpochMilli)

      (auxiliaryAtomEvent, contentAtomEvent)
    }

    AtomPublisher.send(atomEvents)(config)
  }
}
