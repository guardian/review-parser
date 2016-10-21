package com.gu.restaurant_review_parser.parsers.marinaOLoughlin

import com.gu.restaurant_review_parser.ParsedRestaurantReview.NoApproximateLocation
import com.gu.restaurant_review_parser.WebTitle
import com.gu.restaurant_review_parser.parsers.MarinaOLoughlinReviewParser
import org.scalatest._

class MarinaOLoughlinParseNameAndLocationSpec extends FlatSpec with Matchers {

  behavior of "extracting restaurant name and approximate location"

  it should "parse restaurant name and location for 'Restaurant review: Merchants Tavern, London EC2'" in {

    val webTitle = WebTitle("Restaurant review: Merchants Tavern, London EC2")
    val (restaurantName, approxLocation) = MarinaOLoughlinReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.value shouldBe "Merchants Tavern"
    approxLocation.value shouldBe "London EC2"
  }

  it should "parse restaurant name and location for 'Restaurants: Casse-Croûte, London SE1'" in {

    val webTitle = WebTitle("Restaurants: Casse-Croûte, London SE1")
    val (restaurantName, approxLocation) = MarinaOLoughlinReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.value shouldBe "Casse-Croûte"
    approxLocation.value shouldBe "London SE1"
  }

  it should "parse restaurant name and location for 'Restaurant: Gujarati Rasoi, London N16'" in {

    val webTitle = WebTitle("Restaurant: Gujarati Rasoi, London N16")
    val (restaurantName, approxLocation) = MarinaOLoughlinReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.value shouldBe "Gujarati Rasoi"
    approxLocation.value shouldBe "London N16"
  }

  it should "parse restaurant name and location for 'Restaurant Ours, London: ‘not my idea of fun’ – restaurant review'" in {

    val webTitle = WebTitle("Restaurant Ours, London: ‘not my idea of fun’ – restaurant review")
    val (restaurantName, approxLocation) = MarinaOLoughlinReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.value shouldBe "Restaurant Ours"
    approxLocation.value shouldBe "London"
  }

  it should "parse restaurant name and location for 'Hunan, London SW1 – restaurant review | Marina O'Loughlin'" in {
    val webTitle = WebTitle("Hunan, London SW1 – restaurant review | Marina O'Loughlin")
    val (restaurantName, approxLocation) = MarinaOLoughlinReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.value shouldBe "Hunan"
    approxLocation.value shouldBe "London SW1"
  }

  it should "parse restaurant name and location for 'The Croft Kitchen, Biggleswade, Bedfordshire – restaurant review'" in {
    val webTitle = WebTitle("The Croft Kitchen, Biggleswade, Bedfordshire – restaurant review")
    val (restaurantName, approxLocation) = MarinaOLoughlinReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.value shouldBe "The Croft Kitchen"
    approxLocation.value shouldBe "Biggleswade, Bedfordshire"
  }

  it should "parse restaurant name and location for 'Ember Yard, London W1: restaurant review | Marina O'Loughlin'" in {
    val webTitle = WebTitle("Ember Yard, London W1: restaurant review | Marina O'Loughlin")
    val (restaurantName, approxLocation) = MarinaOLoughlinReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.value shouldBe "Ember Yard"
    approxLocation.value shouldBe "London W1"
  }

  it should "parse restaurant name and location for 'Restaurant: Garfunkel's'" in {

    val webTitle = WebTitle("Restaurant: Garfunkel's")
    val (restaurantName, approxLocation) = MarinaOLoughlinReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.value shouldBe "Garfunkel's"
    approxLocation.value shouldBe NoApproximateLocation
  }

  it should "parse restaurant name and location for 'Bronte, London WC2: ‘It doesn’t know whether it’s in Bangkok or Beirut, nor does it much care’ – restaurant review | Marina O’Loughlin'" in {
    val webTitle = WebTitle("Bronte, London WC2: ‘It doesn’t know whether it’s in Bangkok or Beirut, nor does it much care’ – restaurant review | Marina O'Loughlin")
    val (restaurantName, approxLocation) = MarinaOLoughlinReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.value shouldBe "Bronte"
    approxLocation.value shouldBe "London WC2"
  }

}
