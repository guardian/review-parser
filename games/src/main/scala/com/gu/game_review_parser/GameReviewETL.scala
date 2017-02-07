package com.gu.game_review_parser

import java.time.OffsetDateTime

import com.gu.contentapi.client.model.{ItemQuery, SearchQuery}
import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import com.gu.auxiliaryatom.model.auxiliaryatomevent.v1.{AuxiliaryAtom, AuxiliaryAtomEvent, EventType => AuxiliaryAtomEventType}
import integration.{AtomPublisher, ReviewParserConfig}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object GameReviewETL extends App {

  if (args.isEmpty) {
    Console.err.println("Usage: <capi key> [<content id>]")
    Console.err.println("If a content id is not supplied, searches for all game reviews")
    sys.exit(1)
  }

  val stage: String = args(0)
  val itemId: Option[String] = args.lift(1)

  val config = ReviewParserConfig(stage)

  val tags = "tone/reviews,technology/games"
  val showFields = "main,body,byline,creationDate,standfirst,starRating,internalComposerCode,publication"
  val cutOff = DateTime.parse("20140101", DateTimeFormat.forPattern("YYYYMMdd"))

  itemId match {
    case Some(id) =>
      val query = ItemQuery(id)
        .tag(tags)
        .showFields(showFields)
        .showElements("image")
        .fromDate(cutOff)

      val parsed = GameReviewProcessor.processItemQuery(config.capiConfig.capiClient, query)
      if (parsed.nonEmpty) {
        parsed.foreach(p => println(s"Successfully parsed: $p"))
        sendAtoms(parsed)
      }
      else println(s"Failed to parse $id")

    case None =>
      val query = SearchQuery()
        .tag(tags)
        .showFields(showFields)
        .showElements("image")
        .fromDate(cutOff)

      val firstPage = Await.result(config.capiConfig.capiClient.getResponse(query), 5.seconds)
      val pages = 1 to firstPage.pages
      val parsed = GameReviewProcessor.processSearchQuery(pages, config.capiConfig.capiClient, query)

      println(s"Successfully parsed ${parsed.size} game reviews.")

      sendAtoms(parsed)
  }

  config.capiConfig.capiClient.shutdown()

  private def sendAtoms(parsed: Seq[ParsedGameReview]): Unit = {
    val atomEvents: Seq[(AuxiliaryAtomEvent, ContentAtomEvent)] = parsed map { review =>
      val contentAtom = ParsedGameReview.toAtom(review)
      val auxiliaryAtomEvent = AuxiliaryAtomEvent(review.internalComposerCode, eventType = AuxiliaryAtomEventType.Add, Seq(AuxiliaryAtom(contentAtom.id, "review")))
      val contentAtomEvent = ContentAtomEvent(contentAtom, EventType.Update, eventCreationTime = review.creationDate.getOrElse(OffsetDateTime.now).toInstant.toEpochMilli)

      (auxiliaryAtomEvent, contentAtomEvent)
    }
    
    AtomPublisher.send(atomEvents)(config)
  }
}
