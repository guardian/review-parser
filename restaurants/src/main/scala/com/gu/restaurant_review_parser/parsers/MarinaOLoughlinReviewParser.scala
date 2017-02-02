package com.gu.restaurant_review_parser.parsers

import com.gu.restaurant_review_parser._
import com.gu.restaurant_review_parser.parsers.Parser.RestaurantReviewerBasedParser
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import com.gu.restaurant_review_parser.parsers.Delimiters._
import com.gu.restaurant_review_parser.parsers.Rules.{PrefixRule, Rule, SuffixRule}
import utils.PhoneNumberRegexes

import scala.collection.JavaConverters._
import scala.util.matching.Regex


object MarinaOLoughlinReviewParser extends RestaurantReviewerBasedParser[MarinaOLoughlinReviewArticle] {

  val reviewer: String = "Marina O'Loughlin"

  def guessRestaurantNameAndApproximateLocation(webTitle: WebTitle): (Option[RestaurantName], Option[ApproximateLocation]) = {

    val rules: Seq[Rule] = {
      val prefixRules: Seq[PrefixRule] = Seq(
        PrefixRule(webTitlePrefix = "Restaurant review:", junkTextSeparator = ColonDelimiter, restaurantNameAndLocationSeparator = Seq(CommaDelimiter)),
        PrefixRule(webTitlePrefix = "Restaurants:", junkTextSeparator = ColonDelimiter, restaurantNameAndLocationSeparator = Seq(CommaDelimiter)),
        PrefixRule(webTitlePrefix = "Restaurant:", junkTextSeparator = ColonDelimiter, restaurantNameAndLocationSeparator = Seq(CommaDelimiter)))

      val suffixRules: Seq[SuffixRule] = Seq(
        SuffixRule(webTitleSuffix = "restaurant review | Marina O'Loughlin", junkTextSeparator = Seq(ColonDelimiter, HyphenDelimiterWithSpaces), restaurantNameAndLocationSeparator = CommaDelimiter),
        SuffixRule(webTitleSuffix = "restaurant review | Marina O’Loughlin", junkTextSeparator = Seq(ColonDelimiter, HyphenDelimiterWithSpaces), restaurantNameAndLocationSeparator = CommaDelimiter),
        SuffixRule(webTitleSuffix = "– restaurant review  | Marina O’Loughlin", junkTextSeparator = Seq(ColonDelimiter, HyphenDelimiterWithSpaces), restaurantNameAndLocationSeparator = CommaDelimiter),
        SuffixRule(webTitleSuffix = "– restaurant review | Marina o’Loughlin", junkTextSeparator = Seq(ColonDelimiter, HyphenDelimiterWithSpaces), restaurantNameAndLocationSeparator = CommaDelimiter),
        SuffixRule(webTitleSuffix = "| Marina O’Loughlin", junkTextSeparator = Seq(ColonDelimiter, HyphenDelimiterWithSpaces), restaurantNameAndLocationSeparator = CommaDelimiter),
        SuffixRule(webTitleSuffix = "– restaurant review", junkTextSeparator = Seq(ColonDelimiter, HyphenDelimiterWithSpaces), restaurantNameAndLocationSeparator = CommaDelimiter)
      )

      prefixRules ++ suffixRules
    }

    val maybeRuleToApply: Option[Rule] = rules.collectFirst {
      case rule: PrefixRule if PrefixRule.checkPrefixMatches(webTitle, rule).isDefined => rule
      case rule: SuffixRule if SuffixRule.checkSuffixMatches(webTitle, rule).isDefined => rule
    }

    maybeRuleToApply.map {
      case rule: PrefixRule => PrefixRule.applyRule(webTitle, rule)
      case rule: SuffixRule => SuffixRule.applyRule(webTitle, rule)
    }
      .getOrElse {
      (None, None)
    }

  }

  def guessRestaurantWebAddress(articleBody: ArticleBody, restaurantName: RestaurantName): Option[WebAddress] = {
    val doc = Jsoup.parse(articleBody.value)

    val maybeAddress = doc.select("p > a").iterator().asScala.toList
      .filter(e => e.text.toLowerCase.contains(restaurantName.value.toLowerCase)).lastOption // assume the one we want is always the last one.

    maybeAddress.map(elem => WebAddress(elem.attr("href")))
  }

  def guessRatingBreakdown(articleBody: ArticleBody): Option[OverallRating] = {

    val FoodLabel = "Food".toLowerCase
    val AtmosphereLabel = "Atmosphere".toLowerCase
    val ValueForMoneyLabel = "Value for money".toLowerCase

    val doc = Jsoup.parse(articleBody.value)

    val ratingBreakdownElements: List[Element] =
      doc.select("p > strong").iterator().asScala.toList
        .filter { elem =>
          val lowerCaseText = elem.text.toLowerCase
          lowerCaseText.startsWith(FoodLabel) ||
            lowerCaseText.startsWith(AtmosphereLabel) ||
            lowerCaseText.startsWith(ValueForMoneyLabel)
        }

    val ratingBreakdownTypes: List[RatingBreakdownType] = ratingBreakdownElements.flatMap { elem =>
      (elem.nextSibling().toString, elem.text.toLowerCase) match {
        case (rating, label) if label.startsWith(FoodLabel) => Some(FoodRating(rating.trim))
        case (rating, label) if label.startsWith(AtmosphereLabel) => Some(AtmosphereRating(rating.trim))
        case (rating, label) if label.startsWith(ValueForMoneyLabel) => Some(ValueForMoneyRating(rating.trim))
        case _ => None
      }
    }

    for {
      foodRating <- ratingBreakdownTypes.collectFirst { case f: FoodRating => f }
      atmosphereRating <- ratingBreakdownTypes.collectFirst { case a: AtmosphereRating => a }
      valueForMoneyRating <- ratingBreakdownTypes.collectFirst { case v: ValueForMoneyRating => v }
      fRating <- foodRating.rating.split("\\s").collectFirst { case str => OverallRating.returnIfRatingElseNone(str) }.flatten
      aRating <- atmosphereRating.rating.split("\\s").collectFirst { case str => OverallRating.returnIfRatingElseNone(str) }.flatten
      mRating <- valueForMoneyRating.rating.split("\\s").collectFirst { case str => OverallRating.returnIfRatingElseNone(str) }.flatten
    } yield {
      OverallRating(
        minimum = 0,
        actual = {
          val actual = Math.round(Seq(fRating, aRating, mRating).map(_.actual).sum / 3).toShort
          Math.round(actual.toDouble / 2).toShort // we divide by 2 as Marina restaurant reviews give ratings out of 10.
        },
        maximum = 5
      )
    }
  }

  def guessFormattedAddress(articleBody: ArticleBody, restaurantName: RestaurantName): Option[FormattedAddress] = {
    for {
      addressBlockText <- getAddressBlockText(articleBody, restaurantName)
      addr <- addressBlockText.split(DotSpaceDelimiter, 2).headOption
      matchPhoneNumber <- PhoneNumberRegexes.regexes.find(regex => regex.pattern.matcher(addr).matches)
    } yield {
      addr match {
        // TODO: If we decide we would like the telephone number in the model in the future here is where it is exctracted.
        case matchPhoneNumber(num) => FormattedAddress(addr.replace(num, "").trim.stripSuffix(CommaDelimiter))
        case _ => FormattedAddress(addr)
      }
    }
  }

  def guessRestaurantInformation(articleBody: ArticleBody, restaurantName: RestaurantName): Option[RestaurantInformation] = {
    getAddressBlockText(articleBody, restaurantName).flatMap { addressBlockText =>
      addressBlockText.split(DotSpaceDelimiter, 2).lastOption.map(RestaurantInformation)
    }
  }

  private def getAddressBlockText(articleBody: ArticleBody, restaurantName: RestaurantName): Option[String] = {

    def sanitize(input: String): String = input.stripPrefix("•").stripPrefix(".").replaceFirst(",", "").trim

    val doc = Jsoup.parse(articleBody.value)

    val maybeElementContainingAddress = doc.select("a").iterator().asScala.toList
      .filter(e => e.text.toLowerCase.contains(restaurantName.value.toLowerCase)).lastOption // assume the one we want is always the last one.

    maybeElementContainingAddress.map { ma =>
      val nextSibling = ma.nextSibling()
      if (nextSibling.toString.contains("<strong>")) sanitize(nextSibling.nextSibling.toString) else sanitize(nextSibling.toString)

    } orElse {
      doc.select("p").iterator().asScala.toList
        .filter(e => e.text.contains(restaurantName.value)).lastOption
        .map { element =>
          val text = element.text.replace(restaurantName.value, "")
          sanitize(text)
        }
    }

  }

}
