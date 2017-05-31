package com.gu.film_review_parser

import java.time.OffsetDateTime

import com.gu.contentapi.client.model.v1.Content
import com.gu.contentapi.json.CirceDecoders._
import com.gu.film_review_parser.omdb.{OMDB, OMDBData}
import com.gu.film_review_parser.parsers.FilmReviewParser
import org.scalatest.{FunSuite, Matchers}
import utils.JsonHelpers

class FilmReviewParserSpec extends FunSuite with Matchers {
  test("parse a Bad Santa 2 review") {
    val content = JsonHelpers.decodeFromFile[Content]("films/src/test/resources/bad-santa-2.json")
    val parsed = FilmReviewParser.parseContent(content, DummyOMDB)

    val expected = ParsedFilmReview(
      "film/2016/nov/24/bad-santa-2-review-same-old-dirty-tricks",
      "583578d5e4b0e0ad2d75c0f6",
      Some(OffsetDateTime.parse("2016-11-23T11:09:09Z")),
      OffsetDateTime.parse("2016-11-24T22:30:45Z"),
      "Mike McCahill",
      2,
      "Billy Bob Thornton returns in a belated sequel that wrings occasional snickers from a patchy script",
      "Bad Santa 2",
      List("Comedy", "Crime", "Drama"),
      2016,
      "tt1798603",
      List("Mark Waters"),
      List("Billy Bob Thornton", "Kathy Bates", "Tony Cox", "Christina Hendricks"),
      Nil
    )

    parsed should be(Some(expected))
  }

  test("parse a 2013 review") {
    val content = JsonHelpers.decodeFromFile[Content]("films/src/test/resources/kill-your-darlings.json")
    val parsed = FilmReviewParser.parseContent(content, DummyOMDB)

    val expected = ParsedFilmReview(
      "film/2013/dec/05/kill-your-darlings-review",
      "56ad0c12e4b0af7546f71bf0",
      Some(OffsetDateTime.parse("2013-12-04T16:22:45Z")),
      OffsetDateTime.parse("2013-12-05T21:44Z"),
      "Peter Bradshaw",
      3,
      "Daniel Radcliffe shines as Allen Ginsberg in a film that takes a deeper look at the birth of the beat generation",
      "Kill Your Darlings",
      List("Biography", "Drama", "History"),
      2013,
      "tt1311071",
      List("John Krokidas"),
      List("Daniel Radcliffe", "Dane DeHaan", "Michael C. Hall", "Jack Huston"),
      Nil
    )

    parsed should be(Some(expected))
  }

  object DummyOMDB extends OMDB {
    def getData(title: String): Option[OMDBData] = {
      title match {
        case "Kill Your Darlings" => Some(OMDBData(
          genre = List("Biography", "Drama", "History"),
          year = 2013,
          imdbId = "tt1311071",
          directors = List("John Krokidas"),
          actors = List("Daniel Radcliffe", "Dane DeHaan", "Michael C. Hall", "Jack Huston")
        ))
        case "Bad Santa 2" => Some(OMDBData(
          genre = List("Comedy", "Crime", "Drama"),
          year = 2016,
          imdbId = "tt1798603",
          directors = List("Mark Waters"),
          actors = List("Billy Bob Thornton", "Kathy Bates", "Tony Cox", "Christina Hendricks")
        ))
      }
    }
  }
}
