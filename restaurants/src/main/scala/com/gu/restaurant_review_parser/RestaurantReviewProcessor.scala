package com.gu.restaurant_review_parser

import com.google.maps.model.{GeocodingResult}
import com.gu.contentapi.client.GuardianContentClient
import com.gu.contentapi.client.model.v1.SearchResponse
import com.gu.restaurant_review_parser.parsers.Parser.RestaurantReviewerBasedParser

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

object RestaurantReviewProcessor {

  def processRestaurantReviews(pages: Seq[Int], reviewer: Reviewer, capiClient: GuardianContentClient, geocodeFn: String => Array[GeocodingResult]): Seq[ParsedRestaurantReview] = {

    def process(tryResponse: Try[SearchResponse], page: Int): Seq[ParsedRestaurantReview] = {

      import com.gu.restaurant_review_parser.parsers.Parser

      tryResponse match {
        case Success(response) =>
          println(s"Processing page $page of CAPI results")

          val reviewArticles: Seq[ReviewArticle] =
            response.results.map(ReviewArticle(reviewer, _))
              .filterNot(article => reviewer.excludedArticles.contains(article.id))

          reviewer match {
            case MarinaOLoughlin => processPage[MarinaOLoughlinReviewArticle](reviewArticles, geocodeFn)
            case JayRayner => processPage[JayRaynerReviewArticle](reviewArticles, geocodeFn)
            case _ =>
              println(s"Reviewer: ${reviewer.toString} could not be determined. Failed to parse any reviews.")
              Nil
          }

        case Failure(e) =>
          println(s"Skipping page $page because of CAPI failure (${e.getMessage})")
          Nil
      }
    }

    pages.flatMap { page =>
      val tryGetResponse = Try(Await.result(capiClient.getResponse(reviewer.query.page(page)), 5.seconds))
      val parsedRestaurantReviews = process(tryGetResponse, page)
      Thread.sleep(500) // avoid spamming CAPI
      parsedRestaurantReviews
    }
  }

  def processPage[T <: ReviewArticle](reviewArticles: Seq[ReviewArticle], geocode: String => Array[GeocodingResult])(implicit extractor: RestaurantReviewerBasedParser[T]): Seq[ParsedRestaurantReview] = reviewArticles.map { article =>
    println(s"Processing content ${article.id}")

    val webTitle = WebTitle(article.webTitle)
    val maybeStandfirst = article.standfirst map Standfirst
    val (maybeName: Option[RestaurantName], maybeApproxLocation: Option[ApproximateLocation]) = extractor.guessRestaurantNameAndApproximateLocation(webTitle)

    val maybeRatingBreakdown = for {
        body <- article.body
        ratingBreakdown <- extractor.guessRatingBreakdown(ArticleBody(body))
      } yield ratingBreakdown

    val maybeWebAddress = for {
        body <- article.body
        name <- maybeName
        webAddress <- extractor.guessRestaurantWebAddress(ArticleBody(body), name)
      } yield webAddress

    val maybeAddress = for {
        body <- article.body
        name <- maybeName
        address <- extractor.guessFormattedAddress(ArticleBody(body), name)
    } yield address

    val maybeRestaurantInformation = for {
      body <- article.body
      name <- maybeName
      restaurantInformation <- extractor.guessRestaurantInformation(ArticleBody(body), name)
    } yield restaurantInformation

    val maybeAddressInformation: Option[AddressInformation] = maybeAddress.flatMap { addr =>
      Try(geocode(addr.value)(0)).toOption.flatMap { geoResult =>
          val maybeAddressParts = AddressParts(geoResult.addressComponents)
          val location = Location(geoResult.geometry.location.lat, geoResult.geometry.location.lng)
          maybeAddressParts.map(AddressInformation(_, location))
        }
    }

    val parsedRestaurantReview = ParsedRestaurantReview (
      restaurantName = maybeName,
      approximateLocation = maybeApproxLocation,
      reviewer = extractor.reviewer,
      publicationDate = extractor.publicationDate(article.webPublicationDate),
      ratingBreakdown = maybeRatingBreakdown,
      address = maybeAddress,
      addressInformation = maybeAddressInformation,
      restaurantInformation = maybeRestaurantInformation,
      webAddress = maybeWebAddress,
      creationDate = extractor.creationDate(article.creationDate),
      reviewSnippet = extractor.guessReviewSnippet(maybeStandfirst),
      originContentId = article.id,
      internalComposerCode = article.internalComposerCode
    )

    println(parsedRestaurantReview.toString)

    parsedRestaurantReview
  }

}
