package com.gu.game_review_parser

import java.time.OffsetDateTime

import com.gu.contentapi.client.model.v1.Content
import com.gu.contentapi.json.CirceDecoders._
import org.scalatest.{FunSuite, Matchers}
import utils.JsonHelpers

class GameReviewParserSpec extends FunSuite with Matchers {
  test("parse a standard game review") {
    val farCry = JsonHelpers.decodeFromFile[Content]("games/src/test/resources/far-cry-primal.json")
    val parsedList = GameReviewProcessor.processContent(farCry)

    parsedList.length should be(1)

    val parsed = parsedList.head
    parsed.title should be("Far Cry Primal")
    parsed.reviewer should be("Steve Boxer")
    parsed.reviewSnippet should be("Set in the Stone Age, this game is nothing like the previous instalments of the franchise, lacking modern weaponry and an arch-villain")
    parsed.publisher should be(Some("Ubisoft"))
    parsed.pegiRating should be(Some(18))
    parsed.platforms should be (List("PC", "PS4", "Xbox One"))
    parsed.price should be(Some("£40"))
    parsed.rating should be(4)
  }

  test("parse an Observer review") {
    val observer = JsonHelpers.decodeFromFile[Content]("games/src/test/resources/observer_1.json")
    val parsedList = GameReviewProcessor.processContent(observer)

    val review1 = ParsedGameReview(
      contentId = "technology/2016/nov/28/games-reviews-roundup-pokemon-sun-moon-playstation-4-pro-mekazoo",
      internalComposerCode = "5836dbffe4b0da4920d6bb00",
      creationDate = Some(OffsetDateTime.parse("2016-11-24T12:24:31Z")),
      publicationDate = OffsetDateTime.parse("2016-11-28T07:00:23Z"),
      reviewer = "Matt Kamen",
      rating = 5,
      reviewSnippet = "The Pokémon series is ageing like a fine wine; two decades in, and these are its greatest games yet",
      title = "Pokémon Sun and Moon",
      publisher = None,
      platforms = List("3DS"),
      price = None,
      pegiRating = Some(7),
      genre = None)

    val review2 = ParsedGameReview(
      contentId = "technology/2016/nov/28/games-reviews-roundup-pokemon-sun-moon-playstation-4-pro-mekazoo",
      internalComposerCode = "5836dbffe4b0da4920d6bb00",
      creationDate = Some(OffsetDateTime.parse("2016-11-24T12:24:31Z")),
      publicationDate = OffsetDateTime.parse("2016-11-28T07:00:23Z"),
      reviewer = "Will Freeman",
      rating = 4,
      reviewSnippet = "There is little in the way of innovation here, but Mekazoo succeeds, as Kong did, with a captivating, spirited and unashamedly fun example of its genre",
      title = "Mekazoo",
      publisher = None,
      platforms = List("PC", "Mac", "PS4", "Xbox One"),
      price = None,
      pegiRating = Some(7),
      genre = None)

    parsedList.length should be(2)  //Observer articles have 3 reviews, but in this case the 2nd review is not complete
    parsedList(0) should be(review1)
    parsedList(1) should be(review2)
  }

  test("parse another Observer review") {
    val observer = JsonHelpers.decodeFromFile[Content]("games/src/test/resources/observer_2.json")
    val parsedList = GameReviewProcessor.processContent(observer)

    val review1 = ParsedGameReview(
      contentId = "technology/2016/may/30/warhammer-total-war-valkyria-chronicles-doom-games-roundup",
      internalComposerCode = "5746f450e4b062390d4ed9b9",
      creationDate = Some(OffsetDateTime.parse("2016-05-26T13:04:16Z")),
      publicationDate = OffsetDateTime.parse("2016-05-30T06:00:26Z"),
      reviewer = "Matt Kamen",
      rating = 4,
      reviewSnippet = "Given how diverse the existing armies are, this is a minor grievance, but more variety would have made this mix perfect",
      title = "Total War: Warhammer",
      publisher = None,
      platforms = List("PC"),
      price = None,
      pegiRating = Some(16),
      genre = None)

    val review2 = ParsedGameReview(
      contentId = "technology/2016/may/30/warhammer-total-war-valkyria-chronicles-doom-games-roundup",
      internalComposerCode = "5746f450e4b062390d4ed9b9",
      creationDate = Some(OffsetDateTime.parse("2016-05-26T13:04:16Z")),
      publicationDate = OffsetDateTime.parse("2016-05-30T06:00:26Z"),
      reviewer = "Matt Kamen",
      rating = 5,
      reviewSnippet = "Unmissable",
      title = "Valkyria Chronicles: Remastered",
      publisher = None,
      platforms = List("PS4"),
      price = None,
      pegiRating = Some(16),
      genre = None)

    parsedList.length should be(2)
    parsedList(0) should be(review1)
    parsedList(1) should be(review2)
  }
}
