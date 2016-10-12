package com.gu.restaurant_review_parser.parsers.marinaOLoughlin

import com.gu.restaurant_review_parser.{ArticleBody, ParsedRestaurantReview, RestaurantName, WebAddress}
import com.gu.restaurant_review_parser.parsers.{MarinaOLoughlinReviewParser, TestUtils}
import org.scalatest.{FlatSpec, Matchers}

class MarinaOLoughlinParseWebAddressSpec extends FlatSpec with Matchers {

  behavior of "extracting web address"

  it should "Extract the web address from an article for a restaurant." in {
    val articleBody = ArticleBody(TestUtils.resourceToString("articles/lifeandstyle-2012-dec-07-john-salt-london-restaurant-review.txt"))
    val restaurantName = RestaurantName("John Salt")
    val webAddress = MarinaOLoughlinReviewParser.guessRestaurantWebAddress(articleBody, restaurantName)
    webAddress shouldBe Some(WebAddress("http://john-salt.com/"))
  }

  it should "Return nothing when a web address hasn't been found" in {
    val articleBody = ArticleBody(TestUtils.resourceToString("articles/lifeandstyle-2012-dec-07-john-salt-london-restaurant-review.txt"))
    val restaurantName = RestaurantName(ParsedRestaurantReview.NoWebAddress)
    val webAddress = MarinaOLoughlinReviewParser.guessRestaurantWebAddress(articleBody, restaurantName)
    webAddress shouldBe None
  }

}
