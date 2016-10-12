package com.gu.restaurant_review_extractor.parsers.marinaOLoughlin

import com.gu.restaurant_review_extractor._
import com.gu.restaurant_review_extractor.parsers.{MarinaOLoughlinReviewParser, TestUtils}
import org.scalatest.{FlatSpec, Matchers}

class MarinaOLoughlinParseAddressSpec extends FlatSpec with Matchers {

  behavior of "extracting restuaruant address"

  it should "extract the block of text containing the address" in {
    val articleBody = ArticleBody(TestUtils.resourceToString("articles/lifeandstyle-2012-dec-07-john-salt-london-restaurant-review.txt"))
    val restaurantName = RestaurantName("John Salt")
    val address = MarinaOLoughlinReviewParser.guessAddressInformation(articleBody, restaurantName)
    address shouldBe Some(Address("131 Upper Street, London N1, 020-7704 8955. Open dinner, Tue-Sat, 6-10pm; Sat brunch, 10am-3pm; Sun lunch noon-4pm. Set menus: four-course, £28, eight £56, 12 £85, plus drinks and service."))
  }

  it should "extract the block of text containing the address when restaurant name is not a link" in {
    val articleBody = ArticleBody(TestUtils.resourceToString("articles/lifeandstyle-2012-nov-02-the-table-cafe-london-review.txt"))
    val restaurantName = RestaurantName("The Table Cafe")
    val address = MarinaOLoughlinReviewParser.guessAddressInformation(articleBody, restaurantName)
    address shouldBe Some(Address("83 Southwark Street, London SE1, 020-7401 2760. Open all week, 7.30am-5pm (8.30am-4pm Sat & Sun); dinner Thurs-Sat, 5.30-11pm (10.30pm Thurs). Three-course meal with drinks and service, around £40 a head."))
  }

}
