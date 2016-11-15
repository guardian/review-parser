package com.gu.game_review_parser

case class ParsedGameReview(
                             reviewer: String,
                             rating: Int,
                             reviewSnippet: String,
                             title: String,
                             publisher: Option[String] = None,
                             platforms: List[String],
                             price: Option[String] = None,
                             pegiRating: Option[Int] = None,
                             genre: Option[String] = None
                           )
