package com.gu.restaurant_review_parser

import java.time.OffsetDateTime
import com.amazonaws.auth.AWSCredentialsProviderChain
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider.Builder
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.google.maps.GeoApiContext
import com.google.maps.model.GeocodingResult
import com.gu.contentapi.client.GuardianContentClient
import com.gu.contentatom.thrift._
import com.gu.restaurant_review_parser.geocoding.Geocoder
import integration.PorterAtomIntegration
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object ETL extends App {

  if (args.isEmpty) {
    Console.err.println("Usage: <capi key> <google-geocoding-api-key> <atom-stream-name> <sts-role-arn> <reviewer>")
    Console.err.println("Reviewers supported:")
    Console.err.println("Marina O'Loughlin :- type MARINA")
    Console.err.println("Jay Rayner :- type JAY")
    sys.exit(1)
  }

  val capiKey: String = args(0)
  val googleGeoCodingApiKey: String = args(1)
  val atomStreamName: String = args(2)
  val stsRoleArn: String = args(3)
  val reviewer: Reviewer = args(4) match {
    case "MARINA" => MarinaOLoughlin
    case "JAY" => JayRayner
    case _ =>  {
      Console.err.println("reviewer must be one of: [ MARINA, JAY ]")
      sys.exit(1)
    }
  }

  private val kinesisClient = {
    val kinesisCredentialsProvider = new AWSCredentialsProviderChain(
      new ProfileCredentialsProvider("composer"),
      new Builder(stsRoleArn, "contentAtom").build()
    )

    val kinesisClient = new AmazonKinesisClient(kinesisCredentialsProvider)
    kinesisClient.setRegion(Region getRegion Regions.fromName("eu-west-1"))
    kinesisClient
  }

  val capiClient = new GuardianContentClient(capiKey)
  val geoApiContext: GeoApiContext = new GeoApiContext().setApiKey(googleGeoCodingApiKey)
  val geocodeFn: String => Array[GeocodingResult] = Geocoder.geocode(geoApiContext)
  //val geocodeFn: String => Array[GeocodingResult] = (_: String) => Array.empty[GeocodingResult] // used for local development to prevent getting rate limited.

  try {
    val firstPage = Await.result(capiClient.getResponse(reviewer.query), 5.seconds)
    val pages = 1 to firstPage.pages
    val parsedRestaurantReviews: Seq[ParsedRestaurantReview] = RestaurantReviewProcessor.processRestaurantReviews(pages, reviewer, capiClient, geocodeFn)

    println(s"We have successfully parsed ${parsedRestaurantReviews.size} restaurant reviews out of a total of ${firstPage.total} [${reviewer.excludedArticles.size} excluded].")

    val filteredParsedRestaurantReviews = ParsedRestaurantReviewFilter.filter(parsedRestaurantReviews)

    println(s"After filtering, we have ${filteredParsedRestaurantReviews.size} restaurant reviews to publish.")

    val atomEvents = filteredParsedRestaurantReviews map { review =>
      val contentAtom = ParsedRestaurantReview.toAtom(review)
      ContentAtomEvent(contentAtom, EventType.Update, eventCreationTime = review.creationDate.getOrElse(OffsetDateTime.now).toInstant.toEpochMilli)
    }

    println(s"After converting to atoms, we have ${atomEvents.size} atoms for publishing.")

    atomEvents.foreach( event => PorterAtomIntegration.send(event, atomStreamName)(kinesisClient))

    println(s"Finished!")
  } finally {
    capiClient.shutdown()
  }

}
