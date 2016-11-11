package com.gu.restaurant_review_parser

import org.scalatest.{FlatSpec, Matchers}

class ParsedRestaurantReviewTest extends FlatSpec with Matchers {

  it should "generate the same id for an atom given the same source string" in {

    val sourceString = "this-is-my-test-string"
    val expectedIdOne = ParsedRestaurantReview.generateId(sourceString)
    val expectedIdTwo = ParsedRestaurantReview.generateId(sourceString)

    expectedIdOne shouldEqual expectedIdTwo
  }
}