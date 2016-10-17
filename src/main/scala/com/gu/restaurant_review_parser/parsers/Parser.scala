package com.gu.restaurant_review_parser.parsers

import java.time.OffsetDateTime
import com.gu.contentapi.client.model.v1.CapiDateTime
import com.gu.restaurant_review_parser._

object Parser {

  trait RestaurantReviewerBasedParser[T] {

    def reviewer(byline: Option[String]) =  byline.getOrElse("REVIEWER UNKNOWN")
    def publicationDate(webPublicationDate: Option[CapiDateTime]) = webPublicationDate.map(time => OffsetDateTime.parse(time.iso8601)).getOrElse(OffsetDateTime.now)
    def guessRestaurantWebAddress(articleBody: ArticleBody, restaurantName: RestaurantName): Option[WebAddress]
    def guessAddressInformation(articleBody: ArticleBody, restaurantName: RestaurantName): Option[Address]
    def guessRatingBreakdown(articleBody: ArticleBody): Option[RatingBreakdown]
    def guessRestaurantNameAndApproximateLocation(webTitle: WebTitle): (RestaurantName, ApproximateLocation)

  }

  object RestaurantReviewerBasedParser {

    implicit val marinaOLoughlinReviewParser = MarinaOLoughlinReviewParser

  }
}