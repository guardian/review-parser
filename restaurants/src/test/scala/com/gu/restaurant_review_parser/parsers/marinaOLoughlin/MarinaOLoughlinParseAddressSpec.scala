package com.gu.restaurant_review_parser.parsers.marinaOLoughlin

import com.gu.restaurant_review_parser._
import com.gu.restaurant_review_parser.parsers.{MarinaOLoughlinReviewParser, TestUtils}
import org.scalatest.{FlatSpec, Matchers}

class MarinaOLoughlinParseAddressSpec extends FlatSpec with Matchers {

  behavior of "extracting restaurant address"

  it should "extract the address" in {
    val articleBody = ArticleBody(TestUtils.resourceToString("articles/marinaOLoughlin/lifeandstyle-2012-dec-07-john-salt-london-restaurant-review.txt"))
    val restaurantName = RestaurantName("John Salt")
    val address = MarinaOLoughlinReviewParser.guessFormattedAddress(articleBody, restaurantName)
    address shouldBe Some(FormattedAddress("131 Upper Street, London N1"))
  }

  it should "extract the restaurant information" in {
    val articleBody = ArticleBody(TestUtils.resourceToString("articles/marinaOLoughlin/lifeandstyle-2012-dec-07-john-salt-london-restaurant-review.txt"))
    val restaurantName = RestaurantName("John Salt")
    val address = MarinaOLoughlinReviewParser.guessRestaurantInformation(articleBody, restaurantName)
    address shouldBe Some(RestaurantInformation("Open dinner, Tue-Sat, 6-10pm; Sat brunch, 10am-3pm; Sun lunch noon-4pm. Set menus: four-course, £28, eight £56, 12 £85, plus drinks and service."))
  }

  it should "extract the address (2)" in {
    val articleBody = ArticleBody(TestUtils.resourceToString("articles/marinaOLoughlin/lifeandstyle-2012-nov-02-the-table-cafe-london-review.txt"))
    val restaurantName = RestaurantName("The Table Cafe")
    val address = MarinaOLoughlinReviewParser.guessFormattedAddress(articleBody, restaurantName)
    address shouldBe Some(FormattedAddress("83 Southwark Street, London SE1"))
  }

  it should "extract the restaurant information (2)" in {
    val articleBody = ArticleBody(TestUtils.resourceToString("articles/marinaOLoughlin/lifeandstyle-2012-nov-02-the-table-cafe-london-review.txt"))
    val restaurantName = RestaurantName("The Table Cafe")
    val address = MarinaOLoughlinReviewParser.guessRestaurantInformation(articleBody, restaurantName)
    address shouldBe Some(RestaurantInformation("Open all week, 7.30am-5pm (8.30am-4pm Sat & Sun); dinner Thurs-Sat, 5.30-11pm (10.30pm Thurs). Three-course meal with drinks and service, around £40 a head."))
  }

}
