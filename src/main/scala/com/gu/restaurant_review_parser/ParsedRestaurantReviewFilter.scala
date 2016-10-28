package com.gu.restaurant_review_parser

object ParsedRestaurantReviewFilter {

  def filter(parsedRestaurantReviews: Seq[ParsedRestaurantReview]): Seq[ParsedRestaurantReview] = {
    parsedRestaurantReviews.filter(_.restaurantName.isDefined)
  }

}
