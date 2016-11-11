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
    restaurantName.get.value shouldBe "Merchants Tavern"
    approxLocation.get.value shouldBe "London EC2"
  }

  it should "parse restaurant name and location for 'Restaurants: Casse-Croûte, London SE1'" in {

    val webTitle = WebTitle("Restaurants: Casse-Croûte, London SE1")
    val (restaurantName, approxLocation) = MarinaOLoughlinReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Casse-Croûte"
    approxLocation.get.value shouldBe "London SE1"
  }

  it should "parse restaurant name and location for 'Restaurant: Gujarati Rasoi, London N16'" in {

    val webTitle = WebTitle("Restaurant: Gujarati Rasoi, London N16")
    val (restaurantName, approxLocation) = MarinaOLoughlinReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Gujarati Rasoi"
    approxLocation.get.value shouldBe "London N16"
  }

  it should "parse restaurant name and location for 'Restaurant Ours, London: ‘not my idea of fun’ – restaurant review'" in {

    val webTitle = WebTitle("Restaurant Ours, London: ‘not my idea of fun’ – restaurant review")
    val (restaurantName, approxLocation) = MarinaOLoughlinReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Restaurant Ours"
    approxLocation.get.value shouldBe "London"
  }

  it should "parse restaurant name and location for 'Hunan, London SW1 – restaurant review | Marina O'Loughlin'" in {
    val webTitle = WebTitle("Hunan, London SW1 – restaurant review | Marina O'Loughlin")
    val (restaurantName, approxLocation) = MarinaOLoughlinReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Hunan"
    approxLocation.get.value shouldBe "London SW1"
  }

  it should "parse restaurant name and location for 'The Croft Kitchen, Biggleswade, Bedfordshire – restaurant review'" in {
    val webTitle = WebTitle("The Croft Kitchen, Biggleswade, Bedfordshire – restaurant review")
    val (restaurantName, approxLocation) = MarinaOLoughlinReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "The Croft Kitchen"
    approxLocation.get.value shouldBe "Biggleswade, Bedfordshire"
  }

  it should "parse restaurant name and location for 'Ember Yard, London W1: restaurant review | Marina O'Loughlin'" in {
    val webTitle = WebTitle("Ember Yard, London W1: restaurant review | Marina O'Loughlin")
    val (restaurantName, approxLocation) = MarinaOLoughlinReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Ember Yard"
    approxLocation.get.value shouldBe "London W1"
  }

  it should "parse restaurant name and location for 'Restaurant: Garfunkel's'" in {

    val webTitle = WebTitle("Restaurant: Garfunkel's")
    val (restaurantName, approxLocation) = MarinaOLoughlinReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Garfunkel's"
    approxLocation shouldBe None
  }

  it should "parse restaurant name and location for 'Bronte, London WC2: ‘It doesn’t know whether it’s in Bangkok or Beirut, nor does it much care’ – restaurant review | Marina O’Loughlin'" in {
    val webTitle = WebTitle("Bronte, London WC2: ‘It doesn’t know whether it’s in Bangkok or Beirut, nor does it much care’ – restaurant review | Marina O'Loughlin")
    val (restaurantName, approxLocation) = MarinaOLoughlinReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Bronte"
    approxLocation.get.value shouldBe "London WC2"
  }

  it should "parse restaurant name and location for 'Cha Chaan Teng, London WC2: ‘This is frankenfood’ – restaurant review | Marina O’Loughlin'" in {
    val webTitle = WebTitle("Cha Chaan Teng, London WC2: ‘This is frankenfood’ – restaurant review | Marina O’Loughlin")
    val (restaurantName, approxLocation) = MarinaOLoughlinReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Cha Chaan Teng"
    approxLocation.get.value shouldBe "London WC2"
  }

  it should "parse restaurant name and location for 'Takahashi, London: ‘We get nigiri of absolute luxury’ – restaurant review  | Marina O’Loughlin'" in {
    val webTitle = WebTitle("Takahashi, London: ‘We get nigiri of absolute luxury’ – restaurant review  | Marina O’Loughlin")
    val (restaurantName, approxLocation) = MarinaOLoughlinReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Takahashi"
    approxLocation.get.value shouldBe "London"
  }

  it should "parse restaurant name and location for ‘Galvin Hop, London E1: ‘Sausage rolls and pork scratchings and scotch eggs, the platonic ideal of each’ | Marina O’Loughlin'" in {
    val webTitle = WebTitle("Galvin Hop, London E1: ‘Sausage rolls and pork scratchings and scotch eggs, the platonic ideal of each’ | Marina O’Loughlin")
    val (restaurantName, approxLocation) = MarinaOLoughlinReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Galvin Hop"
    approxLocation.get.value shouldBe "London E1"
  }

  it should "parse restaurant name and location for ‘The Hotel Portmeirion: ‘It’s the living definition of trying too hard’ – restaurant review | Marina o’Loughlin'" in {
    val webTitle = WebTitle("The Hotel Portmeirion: ‘It’s the living definition of trying too hard’ – restaurant review | Marina o’Loughlin")
    val (restaurantName, approxLocation) = MarinaOLoughlinReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "The Hotel Portmeirion"
    approxLocation shouldBe None
  }


}
