package com.gu.restaurant_review_extractor.parsers

import com.gu.restaurant_review_extractor._
import com.gu.restaurant_review_extractor.parsers.Parser.RestaurantReviewerBasedParser
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import com.gu.restaurant_review_extractor.parsers.Delimiters._
import com.gu.restaurant_review_extractor.ParsedRestaurantReview._

import scala.annotation.tailrec
import scala.util.Try
import scala.collection.JavaConverters._

object MarinaOLoughlinReviewParser extends RestaurantReviewerBasedParser[MarinaOLoughlinReviewArticle] {

  case class PrefixRule(webTitlePrefix: String, restaurantNameAndLocationSeperator: String, locationAndGarbageSeperator: String)
  object PrefixRule {
    def checkPrefixMatches(webTitle: WebTitle, rule: PrefixRule): Option[PrefixRule] = if (webTitle.value.startsWith(rule.webTitlePrefix)) Some(rule) else None

    def applyRule(webTitle: WebTitle, rule: PrefixRule): (RestaurantName, ApproximateLocation) = {
      val items = webTitle.value.replace(rule.webTitlePrefix, "").split(rule.restaurantNameAndLocationSeperator).take(2).map(_.trim).toSeq
      val name = items.headOption.getOrElse(NoRestaurantName)
      val location = if (items.headOption == items.lastOption) NoApproximateLocation else items.lastOption.flatMap(_.split(rule.locationAndGarbageSeperator).headOption).getOrElse(NoApproximateLocation)
      (RestaurantName(name.trim), ApproximateLocation(location.trim))
    }
  }

  def guessRestaurantNameAndApproximateLocation(webTitle: WebTitle): (RestaurantName, ApproximateLocation) = {

    def guessRestaurantName(webTitle: WebTitle): RestaurantName = {

      @tailrec
      def exec(webTitle: WebTitle, delimiters: Seq[String]): String = {
        val items = delimiters.headOption.map(delimiter => webTitle.value.split(delimiter)).getOrElse(Array.empty)
        if (items.isEmpty) // tried all delimiters without success - can't parse restaurant name
          NoRestaurantName
        else if (items.length == 1) // try another delimiter
          exec(webTitle, delimiters.tail)
        else items(0).replace("review", "").trim // think we've found it - make sure it doesn't contain the word 'review'.
      }

      val restaurantName = exec(webTitle, Seq(CommaDelimiter, ColonDelimiter, HyphenDelimiterWithSpaces))
      RestaurantName(restaurantName.trim)
    }

    def guessLocation(webTitle: WebTitle): ApproximateLocation = {

      @tailrec
      def exec(webTitle: WebTitle, delimiters: Seq[String]): String = {
        val items = webTitle.value.split(CommaDelimiter)

        if (items.length > 2) { // probably formatted like: The Riz, Margate, Kent – restaurant review
          items(1).trim
        } else {
          val itemsForLocation = delimiters.headOption.flatMap(delimiter => Try(items(1).split(delimiter)).toOption).getOrElse(Array.empty)

          if (itemsForLocation.length > 1) {
            val approximateLocation = itemsForLocation.headOption.getOrElse(NoApproximateLocation)
            approximateLocation
          } else if (delimiters.tail.nonEmpty) {
            exec(webTitle, delimiters.tail)
          } else {
            NoApproximateLocation
          }
        }

      }

      val delimiters: Seq[String] = if (!webTitle.value.contains(ColonDelimiter)) Seq(HyphenDelimiterWithSpaces) else Seq(ColonDelimiter, HyphenDelimiterWithSpaces)
      val approximateLocation = exec(webTitle, delimiters)
      ApproximateLocation(approximateLocation.trim)
    }

    val prefixRules: Seq[PrefixRule] = Seq(
      PrefixRule(webTitlePrefix = "Restaurant review:", restaurantNameAndLocationSeperator = CommaDelimiter, locationAndGarbageSeperator = PipeDelimiterWithSpaces),
      PrefixRule(webTitlePrefix = "Restaurants:", restaurantNameAndLocationSeperator = CommaDelimiter, locationAndGarbageSeperator = PipeDelimiterWithSpaces),
      PrefixRule(webTitlePrefix = "Restaurant:", restaurantNameAndLocationSeperator = CommaDelimiter, locationAndGarbageSeperator = PipeDelimiterWithSpaces),
      PrefixRule(webTitlePrefix = "Restaurant ", restaurantNameAndLocationSeperator = CommaDelimiter, locationAndGarbageSeperator = ColonDelimiter)
    )

    val maybeRuleToApply: Option[PrefixRule] = prefixRules.collectFirst { case rule if PrefixRule.checkPrefixMatches(webTitle, rule).isDefined => rule }

    maybeRuleToApply.map(PrefixRule.applyRule(webTitle, _)).getOrElse {
      val name = guessRestaurantName(webTitle)
      val approxLocation = guessLocation(webTitle)
      (name, approxLocation)
    }

  }

  def guessRestaurantWebAddress(articleBody: ArticleBody, restaurantName: RestaurantName): Option[WebAddress] = {
    val doc = Jsoup.parse(articleBody.value)

    val maybeAddress = doc.select("p > a").iterator().asScala.toList
      .filter(e => e.text.toLowerCase.contains(restaurantName.value.toLowerCase)).lastOption // assume the one we want is always the last one.

    maybeAddress.map(elem => WebAddress(elem.attr("href")))
  }

  def guesstRatingBreakdown(articleBody: ArticleBody): Option[RatingBreakdown] = {
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
    } yield {
      RatingBreakdown(foodRating, atmosphereRating, valueForMoneyRating)
    }
  }

  def guessAddressInformation(articleBody: ArticleBody, restaurantName: RestaurantName): Option[Address] = {

    def sanitize(input: String): String = input.stripPrefix("•").stripPrefix(".").replaceFirst(",", "").trim

    val doc = Jsoup.parse(articleBody.value)

    val maybeAddress = doc.select("a").iterator().asScala.toList
      .filter(e => e.text.toLowerCase.contains(restaurantName.value.toLowerCase)).lastOption // assume the one we want is always the last one.

    maybeAddress.map { ma =>
      val nextSibling = ma.nextSibling()
      if (nextSibling.toString.contains("<strong>")) {
        Address(sanitize(nextSibling.nextSibling().toString))
      } else Address(sanitize(nextSibling.toString))

    } orElse {
      doc.select("p").iterator().asScala.toList
        .filter(e => e.text.contains(restaurantName.value)).lastOption
        .map { element =>
          val address = element.text.replace(restaurantName.value, "")
          Address(sanitize(address))
        }
    }
  }

}
