package com.gu.restaurant_review_parser

import java.time.OffsetDateTime

import com.google.maps.GeoApiContext
import com.google.maps.model.GeocodingResult
import com.gu.auxiliaryatom.model.auxiliaryatomevent.v1.{AuxiliaryAtom, AuxiliaryAtomEvent, EventType => AuxiliaryAtomEventType}
import com.gu.contentatom.thrift._
import com.gu.restaurant_review_parser.geocoding.Geocoder
import integration.{AtomPublisher, ReviewParserConfig}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object ETL extends App {

  if (args.isEmpty) {
    Console.err.println("Usage: <stage> <googleGeoCodingApiKey> <reviewer>")
    Console.err.println("Reviewers supported:")
    Console.err.println("Marina O'Loughlin :- type MARINA")
    Console.err.println("Jay Rayner :- type JAY")
    sys.exit(1)
  }

  val stage: String = args(0)
  val googleGeoCodingApiKey: String = args(1)
  val reviewer: Reviewer = args(2) match {
    case "MARINA" => MarinaOLoughlin
    case "JAY" => JayRayner
    case _ =>  {
      Console.err.println("reviewer must be one of: [ MARINA, JAY ]")
      sys.exit(1)
    }
  }

  val config = ReviewParserConfig(stage)
  val geoApiContext: GeoApiContext = new GeoApiContext().setApiKey(googleGeoCodingApiKey)
  val geocodeFn: String => Array[GeocodingResult] = Geocoder.geocode(geoApiContext)
  //val geocodeFn: String => Array[GeocodingResult] = (_: String) => Array.empty[GeocodingResult] // used for local development to prevent getting rate limited.

  try {
    val firstPage = Await.result(config.capiConfig.capiClient.getResponse(reviewer.query), 5.seconds)
      val pages = 1 to firstPage.pages
    val parsedRestaurantReviews: Seq[ParsedRestaurantReview] = RestaurantReviewProcessor.processRestaurantReviews(pages, reviewer, config.capiConfig.capiClient, geocodeFn)

    println(s"We have successfully parsed ${parsedRestaurantReviews.size} restaurant reviews out of a total of ${firstPage.total} [${reviewer.excludedArticles.size} excluded].")
    val filteredParsedRestaurantReviews = ParsedRestaurantReviewFilter.filter(parsedRestaurantReviews)

    println(s"After filtering, we have ${filteredParsedRestaurantReviews.size} restaurant reviews to publish.")

    println(s"The ids of the reviews that are not ready to publish are: ${parsedRestaurantReviews diff filteredParsedRestaurantReviews map(_.originContentId)}")
    val atomEvents: Seq[(AuxiliaryAtomEvent, ContentAtomEvent)] = filteredParsedRestaurantReviews flatMap { review =>

      for {
        internalComposerCode <- review.internalComposerCode
        contentAtom <- ParsedRestaurantReview.toAtom(review)
      } yield {
        val auxiliaryAtomEvent = AuxiliaryAtomEvent(internalComposerCode, eventType = AuxiliaryAtomEventType.Add, Seq(AuxiliaryAtom(contentAtom.id, "review")))
        val contentAtomEvent = ContentAtomEvent(contentAtom, EventType.Update, eventCreationTime = review.creationDate.getOrElse(OffsetDateTime.now).toInstant.toEpochMilli)

        (auxiliaryAtomEvent, contentAtomEvent)
      }

    }

    println(s"After converting to atoms, we have ${atomEvents.size} atoms for publishing.")
    AtomPublisher.send(atomEvents)(config)

    println(s"Finished!")
  } finally {
    config.capiConfig.capiClient.shutdown()
  }

}

