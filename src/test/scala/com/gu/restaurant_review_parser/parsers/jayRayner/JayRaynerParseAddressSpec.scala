package com.gu.restaurant_review_parser.parsers.jayRayner

import com.gu.restaurant_review_parser.{ArticleBody, FormattedAddress, RestaurantName}
import com.gu.restaurant_review_parser.parsers.{JayRaynerReviewParser, MarinaOLoughlinReviewParser, TestUtils}
import org.scalatest.{FlatSpec, Matchers}

class JayRaynerParseAddressSpec extends FlatSpec with Matchers {

  behavior of "extracting restaurant address"

  it should "parse address when address is in the first paragraph and marked up with a <strong> label." in {
    val articleBody = ArticleBody(TestUtils.resourceToString("articles/jayRayner/lifeandstyle-2003-may-11-foodanddrink.shopping6.txt"))
    val restaurantName = RestaurantName("")
    val address = JayRaynerReviewParser.guessFormattedAddress(articleBody, restaurantName)
    address shouldBe Some(FormattedAddress("Grosvenor Square, London W1, 020 7596 3444"))
  }

  it should "parse address when address and telephone information are in seperate <p> tags, marked up with a <strong> label." in {
    val articleBody = ArticleBody(TestUtils.resourceToString("articles/jayRayner/lifeandstyle-2001-aug-19-foodanddrink.restaurants.txt"))
    val restaurantName = RestaurantName("")
    val address = JayRaynerReviewParser.guessFormattedAddress(articleBody, restaurantName)
    address shouldBe Some(FormattedAddress("La Trouvaille, 12A Newburgh Street, London W1, 020 7287 8488"))
  }

  it should "parse address when address is in first paragraph in bold." in {
    val articleBody = ArticleBody(TestUtils.resourceToString("articles/jayRayner/lifeandstyle-2016-oct-16-mercato-metropolitano-restaurant-review-jay-rayner.txt"))
    val restaurantName = RestaurantName("")
    val address = JayRaynerReviewParser.guessFormattedAddress(articleBody, restaurantName)
    address shouldBe Some(FormattedAddress("Mercato Metropolitano, 42 Newington Causeway, London SE1 6DR"))
  }

}
