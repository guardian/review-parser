package com.gu.restaurant_review_parser.parsers

import java.time.OffsetDateTime
import com.google.maps.model._
import com.gu.contentapi.client.model.v1.CapiDateTime
import com.gu.restaurant_review_parser._
import com.gu.restaurant_review_parser.parsers.geocoding.TestGeocodingResultCreator._
import org.scalatest._

class RestaurantReviewProcessorSpec extends FunSuite with Matchers {

  test("We can pass restaurant reviews by Marina O'Loughlin") {

    val webPublicatioDate = "2012-12-07T23:00:07Z"
    val creationDate = "2012-10-07T12:00:00Z"

    val articles = Seq(
      MarinaOLoughlinReviewArticle(
        id = "lifeandstyle/2012/dec/07/john-salt-london-restaurant-review",
        webTitle = "Restaurant: John Salt, London N1",
        byline = Some("Marina O'Loughlin"),
        body = Some(TestUtils.resourceToString("articles/marinaOLoughlin/lifeandstyle-2012-dec-07-john-salt-london-restaurant-review.txt")),
        webPublicationDate = Some(CapiDateTime(OffsetDateTime.parse(webPublicatioDate).toInstant.toEpochMilli, webPublicatioDate)),
        creationDate = Some(CapiDateTime(OffsetDateTime.parse(creationDate).toInstant.toEpochMilli, creationDate)),
        standfirst = Some("As everyone in the room applies tongues to bricks, all I think is, someone's having a laugh")
      )
    )

    val geoResult = Array(geocodingResult(
      formattedAddress = "131 Upper St, Islington, London N1, UK",
      lat = 51.5390429,
      lng = -0.1026274,
      addressComponents = Array(
        addressComponent("131", "131", Array(AddressComponentType.STREET_NUMBER)),
        addressComponent("Upper Street", "Upper St", Array(AddressComponentType.ROUTE)),
        addressComponent("Islington", "Islington", Array(AddressComponentType.NEIGHBORHOOD)),
        addressComponent("London", "London", Array(AddressComponentType.LOCALITY)),
        addressComponent("N1", "N1", Array(AddressComponentType.POSTAL_CODE)),
        addressComponent("London", "London", Array(AddressComponentType.POSTAL_TOWN)),
        addressComponent("United Kingdom", "GB", Array(AddressComponentType.COUNTRY)),
        addressComponent("England", "England", Array(AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1)),
        addressComponent("Greater London", "Greater London", Array(AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_2))
      )
    ))

    import com.gu.restaurant_review_parser.parsers.Parser

    val parsedRestaurantReviews = RestaurantReviewProcessor.processPage[MarinaOLoughlinReviewArticle](articles, (_ :String) => geoResult)
    parsedRestaurantReviews shouldNot be(empty)
    parsedRestaurantReviews.foreach { review =>

      review.restaurantName.get shouldBe RestaurantName("John Salt")
      review.webAddress shouldBe Some(WebAddress("http://john-salt.com/"))
      review.ratingBreakdown shouldBe Some(OverallRating(0, 7, 10))
      review.publicationDate shouldBe OffsetDateTime.parse(webPublicatioDate)
      review.creationDate shouldBe Some(OffsetDateTime.parse(creationDate))
      review.address shouldBe Some(FormattedAddress("131 Upper Street, London N1, 020-7704 8955"))
      review.addressInformation shouldBe Some(AddressInformation(AddressParts(Some(StreetNumber("131")),Some(Route("Upper Street")),Some(Neighborhood("Islington")),Some(Locality("London")),Some(PostalCode("N1")),Some(PostalTown("London")),Some(Country("United Kingdom")),Some(AdministrativeAreaLevelOne("England")),Some(AdministrativeAreaLevelTwo("Greater London"))),Location(51.5390429,-0.1026274)))
      review.restaurantInformation shouldBe Some(RestaurantInformation("Open dinner, Tue-Sat, 6-10pm; Sat brunch, 10am-3pm; Sun lunch noon-4pm. Set menus: four-course, £28, eight £56, 12 £85, plus drinks and service."))
      review.approximateLocation.get shouldBe ApproximateLocation("London N1")
      review.reviewSnippet shouldBe Some(ReviewSnippet("As everyone in the room applies tongues to bricks, all I think is, someone's having a laugh"))
      review.reviewer shouldBe "Marina O'Loughlin"

    }

  }

}
