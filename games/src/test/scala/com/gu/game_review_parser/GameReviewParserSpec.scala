package com.gu.game_review_parser

import com.gu.contentapi.client.model.v1.Content
import com.gu.contentapi.json.CirceDecoders._
import com.gu.game_review_parser.parsers.StandardParser
import org.scalatest.{FunSuite, Matchers}
import utils.JsonHelpers

class GameReviewParserSpec extends FunSuite with Matchers {
  test("parse a standard game review") {
    val farCry = JsonHelpers.decodeFromFile[Content]("games/src/test/resources/far-cry-primal.json")
    val parsed = StandardParser.parseContent(farCry).get

    parsed.title should be("Far Cry Primal")
    parsed.reviewer should be("Steve Boxer")
    parsed.reviewSnippet should be("Set in the Stone Age, this game is nothing like the previous instalments of the franchise, lacking modern weaponry and an arch-villain")
    parsed.publisher should be(Some("Ubisoft"))
    parsed.pegiRating should be(Some(18))
    parsed.platforms should be (List("PC", "PS4", "Xbox One"))
    parsed.price should be(Some("£40"))
    parsed.rating should be(4)
  }
}
