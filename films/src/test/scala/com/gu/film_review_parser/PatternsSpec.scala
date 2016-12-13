package com.gu.film_review_parser

import com.gu.film_review_parser.parsers.FilmReviewParser
import org.scalatest.{FunSuite, Matchers}

class PatternsSpec extends FunSuite with Matchers {
  test("match 'Titanic review - astonishing'") {
    FilmReviewParser.getTitle("Titanic review - astonishing") should be(Some("Titanic"))
  }

  test("match 'Titanic - review'") {
    FilmReviewParser.getTitle("Titanic - review") should be(Some("Titanic"))
  }

  test("match 'Film review: Titanic'") {
    FilmReviewParser.getTitle("Film review: Titanic") should be(Some("Titanic"))
  }

  test("not match 'This is not a proper film review'") {
    FilmReviewParser.getTitle("This is not a proper film review") should be(None)
  }

  test("not match 'This is not: a proper film review'") {
    FilmReviewParser.getTitle("This is not: a proper film review") should be(None)
  }

  test("not match standfirst of '(Cert 15)'") {
    FilmReviewParser.getReviewSnippet("(Cert 15)") should be(None)
  }

  test("match standfirst of 'This is a good reviewSnippet'") {
    FilmReviewParser.getReviewSnippet("This is a good reviewSnippet") should be(Some("This is a good reviewSnippet"))
  }
}
