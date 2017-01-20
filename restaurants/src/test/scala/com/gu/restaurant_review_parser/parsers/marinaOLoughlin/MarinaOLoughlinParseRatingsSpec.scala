package com.gu.restaurant_review_parser.parsers.marinaOLoughlin

import com.gu.restaurant_review_parser._
import com.gu.restaurant_review_parser.parsers.{MarinaOLoughlinReviewParser, TestUtils}
import org.scalatest._

class MarinaOLoughlinParseRatingsSpec extends FlatSpec with Matchers {

  behavior of "extracting ratings breakdowns"

  it should "Extract the ratings breakdown given for a restaurant" in {
    val articleBody = ArticleBody(TestUtils.resourceToString("articles/marinaOLoughlin/lifeandstyle-2014-apr-18-timberyard-edinburgh-restaurant-review-oloughlin-marina.txt"))
    val ratingsBreakdown = MarinaOLoughlinReviewParser.guessRatingBreakdown(articleBody)
    ratingsBreakdown shouldBe
      Some(OverallRating(minimum = 0, actual = 4, maximum = 5))
  }

  it should "Not extract a ratings breakdown when there isn't one" in {
    val articleBody = ArticleBody(TestUtils.resourceToString("articles/marinaOLoughlin/lifeandstyle-2012-aug-31-brasserie-zedel-london-w1-review.txt"))
    val ratingsBreakdown = MarinaOLoughlinReviewParser.guessRatingBreakdown(articleBody)
    ratingsBreakdown shouldBe None
  }

}