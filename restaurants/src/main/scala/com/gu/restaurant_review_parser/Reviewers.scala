package com.gu.restaurant_review_parser

import com.gu.contentapi.client.model.SearchQuery
import com.gu.contentapi.client.model.v1.{CapiDateTime, Content, Element}

sealed trait Reviewer {
  val query: SearchQuery
  val excludedArticles: Seq[String]
}

case object MarinaOLoughlin extends Reviewer {
  val query: SearchQuery = SearchQuery()
    .pageSize(200)
    .contentType("article")
    .tag("lifeandstyle/series/marina-o-loughlin-on-restaurants")
    .showFields("main,body,byline,creationDate,standfirst,internalComposerCode")
    .showElements("image")

  val excludedArticles = Seq(
    "lifeandstyle/2015/oct/24/marina-oloughlin-top-50-uk-restaurants",
    "lifeandstyle/2015/jul/04/restaurant-plates-beautiful-complicated-marina-oloughlin",
    "lifeandstyle/wordofmouth/2013/aug/26/restaurant-pet-hates-ruin-my-appetite",
    "lifeandstyle/wordofmouth/2012/aug/31/marina-oloughlin-cards-on-the-table"
  )
}

case object JayRayner extends Reviewer {
  val query: SearchQuery = SearchQuery()
    .pageSize(200)
    .contentType("article")
    .tag("lifeandstyle/series/jayrayner")
    .showFields("main,body,byline,creationDate,standfirst,internalComposerCode")
    .showElements("image")

  val excludedArticles = Seq(
    "lifeandstyle/2009/mar/01/jay-rayner-cheap-eats",
    "lifeandstyle/wordofmouth/2009/jul/08/music-in-restaurants-rayner-lindisfarne",
    "lifeandstyle/2008/apr/13/foodanddrink.restaurants"
  )
}

sealed trait ReviewArticle {
  val id: String
  val webTitle: String
  val byline: Option[String]
  val body: Option[String]
  val webPublicationDate: Option[CapiDateTime]
  val creationDate: Option[CapiDateTime]
  val standfirst: Option[String]
  val internalComposerCode: Option[String]
  val elements: Seq[Element]
}

case class JayRaynerReviewArticle(
                                   id: String,
                                   webTitle: String,
                                   byline: Option[String],
                                   body: Option[String],
                                   webPublicationDate: Option[CapiDateTime],
                                   creationDate: Option[CapiDateTime],
                                   standfirst: Option[String],
                                   internalComposerCode: Option[String],
                                   elements: Seq[Element]) extends ReviewArticle

case class MarinaOLoughlinReviewArticle(
                                         id: String,
                                         webTitle: String,
                                         byline: Option[String],
                                         body: Option[String],
                                         webPublicationDate: Option[CapiDateTime],
                                         creationDate: Option[CapiDateTime],
                                         standfirst: Option[String],
                                         internalComposerCode: Option[String],
                                         elements: Seq[Element]) extends ReviewArticle


object ReviewArticle {

  def apply(reviewer: Reviewer, content: Content): ReviewArticle = {
    val id = content.id
    val webTitle = content.webTitle
    val byline = content.fields.flatMap(_.byline)
    val body = content.fields.flatMap(_.body)
    val webPublicationDate = content.webPublicationDate
    val creationDate = content.fields.flatMap(_.creationDate)
    val standfirst = content.fields.flatMap(_.standfirst)
    val internalComposerCode = content.fields.flatMap(_.internalComposerCode)
    val elements = content.elements.getOrElse(Nil)

    reviewer match {
      case MarinaOLoughlin => MarinaOLoughlinReviewArticle(id, webTitle, byline, body, webPublicationDate, creationDate, standfirst, internalComposerCode, elements)
      case JayRayner => JayRaynerReviewArticle(id, webTitle, byline, body, webPublicationDate, creationDate, standfirst, internalComposerCode, elements)
    }
  }
}





