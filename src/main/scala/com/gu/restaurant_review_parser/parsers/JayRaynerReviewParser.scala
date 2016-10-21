package com.gu.restaurant_review_parser.parsers

import com.gu.restaurant_review_parser._
import com.gu.restaurant_review_parser.parsers.Parser.RestaurantReviewerBasedParser
import com.gu.restaurant_review_parser.parsers.Delimiters._
import com.gu.restaurant_review_parser.ParsedRestaurantReview._
import com.gu.restaurant_review_parser.parsers.Rules.{PrefixRule, Rule, SuffixRule}

object JayRaynerReviewParser extends RestaurantReviewerBasedParser[JayRaynerReviewArticle] {

  val reviewer: String = "Jay Rayner"

  def guessRestaurantNameAndApproximateLocation(webTitle: WebTitle): (RestaurantName, ApproximateLocation) = {

    val rules: Seq[Rule] = {
      val suffixRules: Seq[SuffixRule] = Seq(
        SuffixRule("restaurant review", Seq(ColonDelimiter), CommaDelimiter),
        SuffixRule("restaurant review | Jay Rayner", Seq(ColonDelimiter), CommaDelimiter),
        SuffixRule("| Jay Rayner", Seq(PipeDelimiterWithSpaces), CommaDelimiter)
      )

      val prefixRules: Seq[PrefixRule] = Seq(
        PrefixRule("Restaurant review", ColonDelimiter, Seq(CommaDelimiter)),
        PrefixRule("Restaurants", ColonDelimiter, Seq(CommaDelimiter)),
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
      (RestaurantName(NoRestaurantName), ApproximateLocation(NoApproximateLocation))
    }

  }

  def guessRestaurantWebAddress(articleBody: ArticleBody, restaurantName: RestaurantName): Option[WebAddress] = { None }

  def guessRatingBreakdown(articleBody: ArticleBody): Option[RatingBreakdown] = { None }

  def guessFormattedAddress(articleBody: ArticleBody, restaurantName: RestaurantName): Option[FormattedAddress] = { None }

  def guessRestaurantInformation(articleBody: ArticleBody, restaurantName: RestaurantName): Option[RestaurantInformation] = { None }

}

