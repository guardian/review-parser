package com.gu.restaurant_review_parser.parsers

import java.time.OffsetDateTime

import com.gu.contentapi.client.model.v1.CapiDateTime
import com.gu.restaurant_review_parser._
import org.jsoup.Jsoup

object Parser {

  trait RestaurantReviewerBasedParser[T] {

    val reviewer: String
    def guessReviewSnippet(standfirst: Option[Standfirst]): Option[ReviewSnippet] = standfirst.map(sf => ReviewSnippet(Jsoup.parse(sf.value.stripSuffix("'").stripPrefix("'")).text))
    def creationDate(creationDate: Option[CapiDateTime]) = creationDate.map(time => OffsetDateTime.parse(time.iso8601))
    def publicationDate(webPublicationDate: Option[CapiDateTime]) = webPublicationDate.map(time => OffsetDateTime.parse(time.iso8601)).getOrElse(OffsetDateTime.now)
    def guessRestaurantWebAddress(articleBody: ArticleBody, restaurantName: RestaurantName): Option[WebAddress]
    def guessFormattedAddress(articleBody: ArticleBody, restaurantName: RestaurantName): Option[FormattedAddress]
    def guessRestaurantInformation(articleBody: ArticleBody, restaurantName: RestaurantName): Option[RestaurantInformation]
    def guessRatingBreakdown(articleBody: ArticleBody): Option[OverallRating]
    def guessRestaurantNameAndApproximateLocation(webTitle: WebTitle): (Option[RestaurantName], Option[ApproximateLocation])

  }

  object RestaurantReviewerBasedParser {

    implicit val marinaOLoughlinReviewParser = MarinaOLoughlinReviewParser
    implicit val jayRaynerReviewParser = JayRaynerReviewParser

  }
}