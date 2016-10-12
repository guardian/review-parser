package com.gu.restaurant_review_extractor

import com.gu.contentapi.client.GuardianContentClient
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object ETL extends App {

  if (args.isEmpty) {
    Console.err.println("Usage: <capi key> <reviewer>")
    Console.err.println("Reviewers supported:")
    Console.err.println("Marina O'Loughlin :- type MARINA")
    sys.exit(1)
  }

  val capiKey: String = args(0)
  val reviewer: Reviewer = args(1) match {
    case "MARINA" => MarinaOLoughlin
    case _ =>  {
      Console.err.println("reviewer must be one of: [ MARINA ]")
      sys.exit(1)
    }
  }

  val capiClient = new GuardianContentClient(capiKey)

  try {
    val firstPage = Await.result(capiClient.getResponse(reviewer.query), 5.seconds)
    val pages = 1 to firstPage.pages
    RestaurantReviewProcessor.processRestaurantReviews(pages, reviewer, capiClient)
    println(s"Finished!")
  } finally {
    capiClient.shutdown()
  }



}
