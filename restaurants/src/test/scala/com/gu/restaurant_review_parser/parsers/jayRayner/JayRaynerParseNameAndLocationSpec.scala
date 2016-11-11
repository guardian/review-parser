package com.gu.restaurant_review_parser.parsers.jayRayner

import com.gu.restaurant_review_parser.WebTitle
import com.gu.restaurant_review_parser.parsers.JayRaynerReviewParser
import org.scalatest._

class JayRaynerParseNameAndLocationSpec extends FlatSpec with Matchers {

  behavior of "extracting restaurant name and approximate location"

  it should "parse restaurant name and location for 'The Talbot Hotel: restaurant review" in {
    val webTitle = WebTitle("The Talbot Hotel: restaurant review")
    val (restaurantName, approxLocation) = JayRaynerReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "The Talbot Hotel"
    approxLocation shouldBe None
  }

  it should "parse restaurant name and location for 'Joe Allen, London: restaurant review" in {
    val webTitle = WebTitle("Joe Allen, London: restaurant review")
    val (restaurantName, approxLocation) = JayRaynerReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Joe Allen"
    approxLocation.get.value shouldBe "London"
  }

  //
  it should "parse restaurant name and location for 'Dalila: restaurant review | Jay Rayner'" in {
    val webTitle = WebTitle("Dalila: restaurant review | Jay Rayner")
    val (restaurantName, approxLocation) = JayRaynerReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Dalila"
    approxLocation shouldBe None
  }

  it should "parse restaurant name and location for 'Made by Bob, Cirencester | Jay Rayner'" in {
    val webTitle = WebTitle("Made by Bob, Cirencester | Jay Rayner")
    val (restaurantName, approxLocation) = JayRaynerReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Made by Bob"
    approxLocation.get.value shouldBe "Cirencester"
  }

  it should "parse restaurant name and location for 'Restaurant review: Newman Street Tavern'" in {
    val webTitle = WebTitle("Restaurant review: Newman Street Tavern")
    val (restaurantName, approxLocation) = JayRaynerReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Newman Street Tavern"
    approxLocation shouldBe None
  }

  it should "parse restaurant name and location for 'Restaurant review: Stovell's, Surrey'" in {
    val webTitle = WebTitle("Restaurant review: Stovell's, Surrey")
    val (restaurantName, approxLocation) = JayRaynerReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Stovell's"
    approxLocation.get.value shouldBe "Surrey"
  }

  it should "parse restaurant name and location for 'Restaurants: Tuscan Steak'" in {
    val webTitle = WebTitle("Restaurants: Tuscan Steak")
    val (restaurantName, approxLocation) = JayRaynerReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Tuscan Steak"
    approxLocation shouldBe None
  }

  it should "parse restaurant name and location for 'Restaurants: Heathers, London SE8'" in {
    val webTitle = WebTitle("Restaurants: Heathers, London SE8")
    val (restaurantName, approxLocation) = JayRaynerReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Heathers"
    approxLocation.get.value shouldBe "London SE8"
  }

  it should "parse restaurant name and location for 'Observer Classic: Oslo Court, London'" in {
    val webTitle = WebTitle("Observer Classic: Oslo Court, London")
    val (restaurantName, approxLocation) = JayRaynerReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Oslo Court"
    approxLocation.get.value shouldBe "London"
  }

  it should "parse restaurant name and location for 'Observer Classic: Oslo Court'" in {
    val webTitle = WebTitle("Observer Classic: Oslo Court")
    val (restaurantName, approxLocation) = JayRaynerReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Oslo Court"
    approxLocation shouldBe None
  }

  it should "parse restaurant name and location for 'Jay Rayner on restaurants: the Ambassadors Hotel'" in {
    val webTitle = WebTitle("Jay Rayner on restaurants: the Ambassadors Hotel")
    val (restaurantName, approxLocation) = JayRaynerReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "the Ambassadors Hotel"
    approxLocation shouldBe None
  }

  it should "parse restaurant name and location for 'Jay Rayner on restaurants: The Black Boy, Oxford'" in {
    val webTitle = WebTitle("Jay Rayner on restaurants: The Black Boy, Oxford")
    val (restaurantName, approxLocation) = JayRaynerReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "The Black Boy"
    approxLocation.get.value shouldBe "Oxford"
  }

  it should "parse restaurant name and location for 'Jay Rayner on 12th House'" in {
    val webTitle = WebTitle("Jay Rayner on 12th House")
    val (restaurantName, approxLocation) = JayRaynerReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "12th House"
    approxLocation shouldBe None
  }

  it should "parse restaurant name and location for 'Jay Rayner on Gastro, London SW4'" in {
    val webTitle = WebTitle("Jay Rayner on Gastro, London SW4")
    val (restaurantName, approxLocation) = JayRaynerReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Gastro"
    approxLocation.get.value shouldBe "London SW4"
  }

  it should "parse restaurant name and location for 'Jay Rayner on the Park Terrace restaurant at the Oriental Mandarin Hotel'" in {
    val webTitle = WebTitle("Jay Rayner on the Park Terrace restaurant at the Oriental Mandarin Hotel")
    val (restaurantName, approxLocation) = JayRaynerReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "the Park Terrace restaurant"
    approxLocation.get.value shouldBe "Oriental Mandarin Hotel"
  }

  it should "parse restaurant name and location for 'Jay Rayner at Chez Bruce, 2 Bellevue Road, London SW17'" in {
    val webTitle = WebTitle("Jay Rayner at Chez Bruce, 2 Bellevue Road, London SW17")
    val (restaurantName, approxLocation) = JayRaynerReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Chez Bruce"
    approxLocation.get.value shouldBe "2 Bellevue Road, London SW17"
  }

  it should "parse restaurant name and location for 'Jay Rayner at the Cinnamon Club'" in {
    val webTitle = WebTitle("Jay Rayner at the Cinnamon Club")
    val (restaurantName, approxLocation) = JayRaynerReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "the Cinnamon Club"
    approxLocation shouldBe None
  }

  it should "parse restaurant name and location for 'Restaurant critic Jay Rayner reviews Maze Grill'" in {
    val webTitle = WebTitle("Restaurant critic Jay Rayner reviews Maze Grill")
    val (restaurantName, approxLocation) = JayRaynerReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Maze Grill"
    approxLocation shouldBe None
  }

  it should "parse restaurant name and location for 'Restaurant critic Jay Rayner reviews Bord'eaux in London'" in {
    val webTitle = WebTitle("Restaurant critic Jay Rayner reviews Bord'eaux in London")
    val (restaurantName, approxLocation) = JayRaynerReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Bord'eaux"
    approxLocation.get.value shouldBe "London"
  }

  it should "parse restaurant name and location for 'Jay Rayner: Montignac'" in {
    val webTitle = WebTitle("Jay Rayner: Montignac")
    val (restaurantName, approxLocation) = JayRaynerReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Montignac"
    approxLocation shouldBe None
  }

  it should "parse restaurant name and location for 'Jay Rayner: Numidie, London'" in {
    val webTitle = WebTitle("Jay Rayner: Numidie, London")
    val (restaurantName, approxLocation) = JayRaynerReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Numidie"
    approxLocation.get.value shouldBe "London"
  }

  it should "parse restaurant name and location for 'Jay Rayner reviews restaurant Quo Vadis, London W1'" in {
    val webTitle = WebTitle("Jay Rayner reviews restaurant Quo Vadis, London W1")
    val (restaurantName, approxLocation) = JayRaynerReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Quo Vadis"
    approxLocation.get.value shouldBe "London W1"
  }

  it should "parse restaurant name and location for 'Jay Rayner reviews Helene Darroze at the Connaught'" in {
    val webTitle = WebTitle("Jay Rayner reviews Helene Darroze at the Connaught")
    val (restaurantName, approxLocation) = JayRaynerReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Helene Darroze"
    approxLocation.get.value shouldBe "the Connaught"
  }

  it should "parse restaurant name and location for 'Jay Rayner reviews The Seahorse in Dartmouth'" in {
    val webTitle = WebTitle("Jay Rayner reviews The Seahorse in Dartmouth")
    val (restaurantName, approxLocation) = JayRaynerReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "The Seahorse"
    approxLocation.get.value shouldBe "Dartmouth"
  }

  it should "parse restaurant name and location for 'Jay Rayner enjoys The Allotment, Dover's answer to the bistro'" in {
    val webTitle = WebTitle("Jay Rayner enjoys The Allotment, Dover's answer to the bistro")
    val (restaurantName, approxLocation) = JayRaynerReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "The Allotment"
    approxLocation.get.value shouldBe "Dover's answer to the bistro"
  }

  it should "parse restaurant name and location for 'Resturants: Sticklebackpink, SW10'" in {
    val webTitle = WebTitle("Resturants: Sticklebackpink, SW10")
    val (restaurantName, approxLocation) = JayRaynerReviewParser.guessRestaurantNameAndApproximateLocation(webTitle)
    restaurantName.get.value shouldBe "Sticklebackpink"
    approxLocation.get.value shouldBe "SW10"
  }

}
