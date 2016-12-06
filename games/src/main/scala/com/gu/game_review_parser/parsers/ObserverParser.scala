package com.gu.game_review_parser.parsers

import java.time.OffsetDateTime

import com.gu.contentapi.client.model.v1.Content
import com.gu.game_review_parser.ParsedGameReview
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.collection.JavaConverters._
import scala.util.Try

/**
  * Observer game review articles contain 3 reviews.
  *
  * These reviews never have prices.
  *
  * They occasionally mention the publisher in the same comma-separated list as platform/pegiRating,
  * but there's no way to reliably identify this unless we hardcode publisher names.
  *
  * We can't use the standfirst for the reviewSnippets, but the last sentence of the review is usually pretty good.
  */
object ObserverParser {
  def parseContent(content: Content): List[ParsedGameReview] = {
    val result = for {
      fields <- content.fields
      body <- fields.body
      internalComposerCode <- fields.internalComposerCode
      creationDate = fields.creationDate.map(time => OffsetDateTime.parse(time.iso8601))
      publicationDate = content.webPublicationDate.map(time => OffsetDateTime.parse(time.iso8601)).getOrElse(OffsetDateTime.now)
      byline <- fields.byline
      reviewers = Reviewers(byline)
    } yield {
      val doc = Jsoup.parse(body)
      doc.select("h2").iterator().asScala.toList.flatMap { h2 =>
        val parsed = parseReview(h2, reviewers) map { details =>

          ParsedGameReview(
            content.id,
            internalComposerCode,
            creationDate,
            publicationDate,
            details.reviewer,
            details.starRating,
            details.reviewSnippet,
            details.title,
            publisher = None,
            platforms = details.platforms,
            price = None,
            pegiRating = details.pegiRating,
            genre = Nil)
        }

        parsed match {
          case Some(p) => println(s"Parsed an Observer review in ${content.id} as $parsed")
          case None => println(s"Failed to parse an Observer review in ${content.id}")
        }
        parsed
      }
    }
    result.getOrElse(Nil)
  }

  private def parseReview(h2: Element, reviewers: Reviewers): Option[Details] = {
    Option(h2.nextElementSibling()) flatMap { nextElement =>
      if (nextElement.tagName == "p") {
        val title = getTitle(h2)
        title.flatMap(getDetails(nextElement, reviewers, _))
      } else None
    }
  }

  private def getTitle(h2: Element): Option[String] = {
    //Expect a single element containing the title - either an <a> or a <strong>
    Try(h2.child(0)).toOption.flatMap { child =>
      if (child.tagName == "a" || child.tagName == "strong") Some(child.text)
      else None
    }
  }

  case class Reviewers(reviewersMap: Map[String,String]) {
    def getName(initials: Option[String]): Option[String] = {
      initials match {
        case Some(i) => reviewersMap.get(i)
        case None =>
          //If there's only one reviewer, it doesn't give the initials
          if (reviewersMap.size == 1) reviewersMap.headOption.map(_._2)
          else None
      }
    }
  }
  object Reviewers {
    //Map for looking up by initials
    def apply(byline: String): Reviewers = {
      val map = byline.split(",|and").map { reviewer =>
        val trimmed = reviewer.trim
        val initials = trimmed.split(" ").map(_.charAt(0)).mkString

        initials -> trimmed
      }.toMap
      Reviewers(map)
    }
  }

  private case class Details(title: String,
                             reviewSnippet: String,
                             reviewer: String,
                             starRating: Int,
                             platforms: List[String],
                             pegiRating: Option[Int])

  private val pegiRatingPattern = """cert: (\d{1,2})""".r
  private val starRatingPattern = "(★{1,5})".r.unanchored

  private def getDetails(p: Element, reviewers: Reviewers, title: String): Option[Details] = {
    val tokens: List[String] = Option(p.select("strong").first) match {
      case Some(strong) =>

        /**
          * <h2> title </h2>
          * <p>
          *   <strong> platforms </strong>
          *   <br/>
          *     ★★★★★
          *   <br/>
          *   ...
          * </p>
          * ...
          */
        strong.text.split(""",|;|/""").map(_.trim).toList

      case None =>

        /**
          * <h2> title </h2>
          * <p> platforms </p>
          * <p> ★★★★★ </p>
          * ...
          */
        Option(p.textNodes).map(nodes => nodes.iterator.asScala.toList.flatMap(_.text.split(""",|;|/""").map(_.trim.replaceAll("""\(|\)""", ""))))
          .getOrElse(Nil)
    }

    parseTokens(p, reviewers, title, tokens)
  }

  private def parseTokens(p: Element, reviewers: Reviewers, title: String, tokens: List[String]): Option[Details] = {
    val platforms = tokens.flatMap(token => ParsedGameReview.platforms.get(token.toLowerCase))
    val pegiRating = tokens.collectFirst { case pegiRatingPattern(age) => age.toInt }

    //Star rating may be in this <p> or the next
    val maybeStarRating = getStarRating(p.text).orElse {
      Option(p.nextElementSibling) flatMap { next =>
        if (next.tagName == "p") getStarRating(next.text)
        else None
      }
    }

    for {
      starRating <- maybeStarRating
      reviewer <- reviewers.getName(getReviewerInitials(p))
      reviewSnippet <- getLastSentence(p)
      if platforms.nonEmpty
    } yield Details(title, reviewSnippet, reviewer, starRating, platforms, pegiRating)
  }

  private def getStarRating(text: String): Option[Int] = {
    text match {
      case starRatingPattern(rating) => Some(rating.length)
      case _ => None
    }
  }

  private def getReviewerInitials(topP: Element): Option[String] = {
    //Get the next <strong> inside a sibling <p>
    def getNextStrongInP(p: Element): Option[Element] = {
      Option(p.select("strong").last).orElse {
        Option(p.nextElementSibling).flatMap { next =>
          if (next.tagName == "p") getNextStrongInP(next)
          else None
        }
      }
    }

    Option(topP.nextElementSibling).flatMap(next => getNextStrongInP(next).map(_.text))
  }

  /**
    * Use the last sentence of the review as the reviewSnippet
    */
  private def getLastSentence(topP: Element): Option[String] = {
    //Get last non-empty <p> before next element which is not <p>
    def getLastP(p: Element): Option[Element] = {
      Option(p.nextElementSibling) match {
        case Some(next) =>
          if (next.tagName == "p" && next.text.nonEmpty) getLastP(next)
          else Some(p)
        case None => Some(p)
      }
    }

    getLastP(topP).flatMap { lastP: Element =>
      //Remove the reviewer's initials at the end
      val copy = lastP.clone
      copy.select("strong").remove
      copy.text.split("""\.\s""").lastOption.map(_.trim.stripSuffix("."))
    }
  }
}
