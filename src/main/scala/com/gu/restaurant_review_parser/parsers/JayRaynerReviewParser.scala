package com.gu.restaurant_review_parser.parsers

import com.gu.restaurant_review_parser._
import com.gu.restaurant_review_parser.parsers.Parser.RestaurantReviewerBasedParser
import com.gu.restaurant_review_parser.parsers.Delimiters._
import com.gu.restaurant_review_parser.ParsedRestaurantReview._
import com.gu.restaurant_review_parser.parsers.Rules.{PrefixRule, Rule, SuffixRule}
import org.jsoup.Jsoup

import scala.collection.JavaConverters._

object JayRaynerReviewParser extends RestaurantReviewerBasedParser[JayRaynerReviewArticle] {

  val reviewer: String = "Jay Rayner"

  def guessRestaurantNameAndApproximateLocation(webTitle: WebTitle): (Option[RestaurantName], Option[ApproximateLocation]) = {

    val rules: Seq[Rule] = {
      val suffixRules: Seq[SuffixRule] = Seq(
        SuffixRule("restaurant review", Seq(ColonDelimiter), CommaDelimiter),
        SuffixRule("restaurant review | Jay Rayner", Seq(ColonDelimiter), CommaDelimiter),
        SuffixRule("| Jay Rayner", Seq(PipeDelimiterWithSpaces), CommaDelimiter)
      )

      val prefixRules: Seq[PrefixRule] = Seq(
        PrefixRule("Restaurant review", ColonDelimiter, Seq(CommaDelimiter)),
        PrefixRule("Restaurants", ColonDelimiter, Seq(CommaDelimiter)),
        PrefixRule("Resturants:", ColonDelimiter, Seq(CommaDelimiter)),
        PrefixRule("Observer Classic", ColonDelimiter, Seq(CommaDelimiter)),
        PrefixRule("Jay Rayner on restaurants:", ColonDelimiter, Seq(CommaDelimiter)),
        PrefixRule("Jay Rayner at", "at", Seq(CommaDelimiter)),
        PrefixRule("Restaurant critic Jay Rayner reviews", " reviews ", Seq(" in ")),
        PrefixRule("Jay Rayner:", ColonDelimiter, Seq(CommaDelimiter)),
        PrefixRule("Jay Rayner reviews restaurant", "restaurant ", Seq(CommaDelimiter)),
        PrefixRule("Jay Rayner enjoys", "enjoys ", Seq(CommaDelimiter)),
        PrefixRule("Jay Rayner on", " on ", Seq(CommaDelimiter, " at the ")),
        PrefixRule("Jay Rayner reviews", "reviews ", Seq(CommaDelimiter, " in ", " at "))
      )

      suffixRules ++ prefixRules
    }

    val maybeRuleToApply: Option[Rule] = rules.collectFirst {
      case rule: SuffixRule if SuffixRule.checkSuffixMatches(webTitle, rule).isDefined => rule
      case rule: PrefixRule if PrefixRule.checkPrefixMatches(webTitle, rule).isDefined => rule
    }

    maybeRuleToApply.map {
      case suffixRule: SuffixRule => SuffixRule.applyRule(webTitle, suffixRule)
      case prefixRule: PrefixRule => PrefixRule.applyRule(webTitle, prefixRule)
    }.getOrElse {
      (None, None)
    }

  }

  def guessRestaurantWebAddress(articleBody: ArticleBody, restaurantName: RestaurantName): Option[WebAddress] = { None }

  def guessRatingBreakdown(articleBody: ArticleBody): Option[OverallRating] =  None  // Jay Rayner reviews don't provide this.

  def guessFormattedAddress(articleBody: ArticleBody, restaurantName: RestaurantName): Option[FormattedAddress] = {

    val TelephoneLabel = "Telephone:"
    val AddressLabel = "Address:"
    val StrongElementType = "strong"

    val doc = Jsoup.parse(articleBody.value)

    def getAddressFromFirstParagraphWhenBold: Option[String] = {
      val maybeAddressBlockFirstBold = Option(doc.select("p").first)
        .filter(p => p.children().size == 1 && p.child(0).tagName == StrongElementType && !p.child(0).text.contains(AddressLabel) && !p.child(0).text.contains(TelephoneLabel))

      maybeAddressBlockFirstBold.flatMap(_.text.split(DotSpaceDelimiter).headOption)
    }

    def getAddressWhenFirstParagraphAndLabelled: Option[String] = {
      val maybeAddressBlockContainsTelephoneAndAddress = Option(doc.select("p").first)
        .filter( p => p.html.contains(TelephoneLabel) && p.html.contains(AddressLabel))

      maybeAddressBlockContainsTelephoneAndAddress.flatMap { item =>
        item.select(StrongElementType).asScala.foreach(_.remove())
        maybeAddressBlockContainsTelephoneAndAddress.map(_.html.split(BrDelimiter).map(_.trim.stripSuffix(DotDelimiter)).reverse.tail.mkString(CommaSpaceDelimiter))
      }
    }

    def getAddressWhenLabelledOverManyParagraphs: Option[String] = {
      val maybeAddressBlockContainsTelephoneOnly = doc.select("p").iterator().asScala.toSeq.find(p => p.html.contains(TelephoneLabel) && !p.html.contains(AddressLabel))
      val maybeAddressBlockContainsAddressOnly = doc.select("p").iterator().asScala.toSeq.find(p => p.html.contains(AddressLabel) && !p.html.contains(TelephoneLabel))

      for {
        address <- maybeAddressBlockContainsAddressOnly
        telephone <- maybeAddressBlockContainsTelephoneOnly
      } yield {
        address.select(StrongElementType).remove()
        telephone.select(StrongElementType).remove()
        s"${address.text().stripSuffix(DotDelimiter).trim}, ${telephone.text.trim}"
      }
    }

    val formattedAddress =
      getAddressFromFirstParagraphWhenBold orElse
        getAddressWhenFirstParagraphAndLabelled orElse
        getAddressWhenLabelledOverManyParagraphs orElse None

    formattedAddress.map(FormattedAddress)
  }

  def guessRestaurantInformation(articleBody: ArticleBody, restaurantName: RestaurantName): Option[RestaurantInformation] = { None }

}

