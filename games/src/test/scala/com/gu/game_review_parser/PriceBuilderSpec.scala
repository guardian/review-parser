package com.gu.game_review_parser

import com.gu.contententity.thrift.Price
import org.scalatest.{FunSuite, Matchers}

class PriceBuilderSpec extends FunSuite with Matchers {
  test("build price with no decimal") {
    val expected = Price("GBP", 4000)
    PriceBuilder("£40") should be(Some(expected))
  }
  test("build price with decimal") {
    val expected = Price("GBP", 4099)
    PriceBuilder("£40.99") should be(Some(expected))
  }
  test("not build price with no currency") {
    PriceBuilder("40.99") should be(None)
  }
}
