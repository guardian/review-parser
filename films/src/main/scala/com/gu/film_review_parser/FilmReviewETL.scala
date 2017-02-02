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
        .showElements("image")

      val parsed = FilmReviewProcessor.processItemQuery(config.capiConfig.capiClient, query)
      if (parsed.successful.nonEmpty) println(s"Parsed $id as: ${parsed.successful}")

      sendAtoms(parsed.successful, false)
      sendAtoms(parsed.failed, true)


    case None =>
      val query = SearchQuery()
        .tag(tags)
        .showFields(showFields)
        .showElements("image")

      val firstPage = Await.result(config.capiConfig.capiClient.getResponse(query), 5.seconds)
      val count = (1 to firstPage.pages).fold(0) { (sum, page) =>
        val parsed = FilmReviewProcessor.processSearchQuery(page, config.capiConfig.capiClient, query)
        sendAtoms(parsed.successful, false)
        sendAtoms(parsed.failed, true)

        val newSum = sum + parsed.successful.length
        println
        println(s"Sent ${parsed.successful.length} more film reviews, total = $newSum")
        newSum
      }

      println(s"Finished processing, sent $count reviews.")
  }

  config.capiConfig.capiClient.shutdown()

  private def sendAtoms(parsed: Seq[ParsedFilmReview], takedown: Boolean = false): Unit = {
    if (parsed.nonEmpty) {
      val atomEvents: Seq[(AuxiliaryAtomEvent, ContentAtomEvent)] = parsed map { review =>
        val contentAtom = ParsedFilmReview.toAtom(review)

        val auxEvent = if (takedown) AuxiliaryAtomEventType.Remove else AuxiliaryAtomEventType.Add
        val auxiliaryAtomEvent = AuxiliaryAtomEvent(review.internalComposerCode, eventType = auxEvent, Seq(AuxiliaryAtom(contentAtom.id, "review")))

        val atomEvent = if (takedown) EventType.Takedown else EventType.Update
        val contentAtomEvent = ContentAtomEvent(contentAtom, atomEvent, eventCreationTime = review.creationDate.getOrElse(OffsetDateTime.now).toInstant.toEpochMilli)

        (auxiliaryAtomEvent, contentAtomEvent)
      }

      println(s"Sending atoms with takedown==$takedown: $parsed")
      AtomPublisher.send(atomEvents)(config)
    }
  }
}
