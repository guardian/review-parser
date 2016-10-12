package com.gu.restaurant_review_extractor.parsers

import java.time.OffsetDateTime
import java.time.temporal.ChronoField

import com.gu.contentapi.client.model.v1.CapiDateTime
import com.gu.restaurant_review_extractor._
import org.scalatest._

class RestaurantReviewProcessorSpec extends FunSuite with Matchers {

  test("We can pass restaurant reviews by Marina O'Loughlin") {

    val webPublicatioDate = "2012-12-07T23:00:07Z"
    val articles = Seq(
      MarinaOLoughlinReviewArticle(
        id = "lifeandstyle/2012/dec/07/john-salt-london-restaurant-review",
        webTitle = "Restaurant: John Salt, London N1",
        byline = Some("Marina O'Loughlin"),
        body = Some(TestUtils.resourceToString("articles/lifeandstyle-2012-dec-07-john-salt-london-restaurant-review.txt")),
        webPublicationDate = Some(CapiDateTime(OffsetDateTime.parse(webPublicatioDate).get(ChronoField.MILLI_OF_SECOND), webPublicatioDate))
      )
    )

    import com.gu.restaurant_review_extractor.parsers.Parser

    val parsedRestaurantReviews = RestaurantReviewProcessor.processPage[MarinaOLoughlinReviewArticle](articles)
    parsedRestaurantReviews shouldNot be(empty)
    parsedRestaurantReviews.foreach { review =>

      review.restaurantName shouldBe RestaurantName("John Salt")
      review.webAddress shouldBe Some(WebAddress("http://john-salt.com/"))
      review.ratingBreakdown shouldBe Some(RatingBreakdown(FoodRating("7/10"), AtmosphereRating("6/10"), ValueForMoneyRating("8/10")))
      review.publicationDate shouldBe OffsetDateTime.parse(webPublicatioDate)
      review.address shouldBe Some(Address("131 Upper Street, London N1, 020-7704 8955. Open dinner, Tue-Sat, 6-10pm; Sat brunch, 10am-3pm; Sun lunch noon-4pm. Set menus: four-course, £28, eight £56, 12 £85, plus drinks and service."))
      review.approximateLocation shouldBe ApproximateLocation("London N1")
      review.reviewer shouldBe "Marina O'Loughlin"

    }


  }

}
