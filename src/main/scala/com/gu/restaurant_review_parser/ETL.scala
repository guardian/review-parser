package com.gu.restaurant_review_parser

import com.google.maps.GeoApiContext
import com.google.maps.model.GeocodingResult
import com.gu.contentapi.client.GuardianContentClient
import com.gu.restaurant_review_parser.geocoding.Geocoder

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object ETL extends App {

  if (args.isEmpty) {
    Console.err.println("Usage: <capi key> <google-geocoding-api-key> <reviewer>")
    Console.err.println("Reviewers supported:")
    Console.err.println("Marina O'Loughlin :- type MARINA")
    sys.exit(1)
  }

  val capiKey: String = args(0)
  val googleGeoCodingApiKey: String = args(1)
  val reviewer: Reviewer = args(2) match {
    case "MARINA" => MarinaOLoughlin
    case _ =>  {
      Console.err.println("reviewer must be one of: [ MARINA ]")
      sys.exit(1)
    }
  }

  val capiClient = new GuardianContentClient(capiKey)
  val geoApiContext: GeoApiContext = new GeoApiContext().setApiKey(googleGeoCodingApiKey)
  val geocodeFn: String => Array[GeocodingResult] = Geocoder.geocode(geoApiContext)

  try {
    val firstPage = Await.result(capiClient.getResponse(reviewer.query), 5.seconds)
    val pages = 1 to firstPage.pages
    RestaurantReviewProcessor.processRestaurantReviews(pages, reviewer, capiClient, geocodeFn)
    println(s"Finished!")
  } finally {
    capiClient.shutdown()
  }



}
