package com.gu.restaurant_review_extractor

import java.time.OffsetDateTime

case class WebAddress(value: String) extends AnyVal
case class Address(value: String) extends AnyVal
case class ArticleBody(value: String) extends AnyVal
case class RestaurantName(value: String) extends AnyVal
case class ApproximateLocation(value: String) extends AnyVal
case class WebTitle(value: String) extends AnyVal

sealed trait RatingBreakdownType {
  val rating: String
}
case class FoodRating(rating: String) extends RatingBreakdownType
case class AtmosphereRating(rating: String) extends RatingBreakdownType
case class ValueForMoneyRating(rating: String) extends RatingBreakdownType

case class RatingBreakdown(food: FoodRating, atmosphere: AtmosphereRating, valueForMoney: ValueForMoneyRating)

case class ParsedRestaurantReview (
  restaurantName: RestaurantName,
  approximateLocation: ApproximateLocation,
  reviewer: String,
  publicationDate: OffsetDateTime,
  ratingBreakdown: Option[RatingBreakdown],
  address: Option[Address], // TODO - structure accordingly
  webAddress: Option[WebAddress]
) {

  override def toString: String = {
    s"Restaurant name: ${restaurantName.value}, \n" +
    s"Rough location: ${approximateLocation.value}, \n" +
    s"Reviewer: $reviewer, \n" +
    s"Publication date: ${publicationDate.toString}, \n" +
    s"Ratings: ${ratingBreakdown.getOrElse(ParsedRestaurantReview.NoRatingBreakdown)}, \n" +
    s"Address: ${address.getOrElse(ParsedRestaurantReview.NoAddress)}, \n" +
    s"Web address: ${webAddress.getOrElse(ParsedRestaurantReview.NoWebAddress)} \n"
  }
}

object ParsedRestaurantReview {
  val NoRestaurantName = "NO RESTAURANT NAME"
  val NoApproximateLocation = "NO APPROXIMATE LOCATION"
  val NoRatingBreakdown = "NO RATING BREAKDOWN"
  val NoAddress = "NO ADDRESS"
  val NoWebAddress = "NO WEB ADDRESS"
}

