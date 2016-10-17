package com.gu.restaurant_review_parser

import com.gu.contentapi.client.model.SearchQuery
import com.gu.contentapi.client.model.v1.{CapiDateTime, Content}

sealed trait Reviewer {
  val query: SearchQuery
  val excludedArticles: Seq[String]
}

case object MarinaOLoughlin extends Reviewer {
  val query: SearchQuery = SearchQuery()
    .pageSize(200)
    .contentType("article")
    .tag("lifeandstyle/series/marina-o-loughlin-on-restaurants")
    .showFields("main,body,byline")

  val excludedArticles = Seq(
    "lifeandstyle/2015/oct/24/marina-oloughlin-top-50-uk-restaurants",
    "lifeandstyle/2015/jul/04/restaurant-plates-beautiful-complicated-marina-oloughlin",
    "lifeandstyle/wordofmouth/2013/aug/26/restaurant-pet-hates-ruin-my-appetite",
    "lifeandstyle/wordofmouth/2012/aug/31/marina-oloughlin-cards-on-the-table"
  )
}

sealed trait ReviewArticle {
  val id: String
  val webTitle: String
  val byline: Option[String]
  val body: Option[String]
  val webPublicationDate: Option[CapiDateTime]
}

object ReviewArticle {
  def apply(reviewer: Reviewer, content: Content): ReviewArticle = {
    val id = content.id
    val webTitle = content.webTitle
    val byline = content.fields.flatMap(_.byline)
    val body = content.fields.flatMap(_.body)
    val webPublicationDate = content.webPublicationDate

    reviewer match {
      case MarinaOLoughlin => MarinaOLoughlinReviewArticle(id, webTitle, byline, body, webPublicationDate)
    }
  }
}

case class MarinaOLoughlinReviewArticle(
   id: String,
   webTitle: String,
   byline: Option[String],
   body: Option[String],
   webPublicationDate: Option[CapiDateTime]) extends ReviewArticle

