package com.gu.restaurant_review_parser.parsers

import com.gu.restaurant_review_parser.ParsedRestaurantReview._
import com.gu.restaurant_review_parser.{ApproximateLocation, RestaurantName, WebTitle}

object Rules {

  sealed trait Rule
  case class SuffixRule(webTitleSuffix: String, junkTextSeparator: Seq[String], restaurantNameAndLocationSeparator: String) extends Rule
  object SuffixRule {
    def checkSuffixMatches(webTitle: WebTitle, rule: SuffixRule): Option[SuffixRule] = if (webTitle.value.endsWith(rule.webTitleSuffix)) Some(rule) else None

    def applyRule(webTitle: WebTitle, rule: SuffixRule): (RestaurantName, ApproximateLocation) = {

      def escape(sep: String): String = if (sep == " | ") "\\|" else sep

      val maybeJunkTextSeparator: Option[String] = rule.junkTextSeparator.collectFirst { case sep if webTitle.value.contains(sep) => sep }

      println("in here")
      println("FOO + " + maybeJunkTextSeparator)

      maybeJunkTextSeparator match {
        case Some(separator) =>
            val items = webTitle.value.split(escape(separator), 2).toSeq
            val doesNameAndLocationSeparatorExist = items.headOption.exists(_.contains(rule.restaurantNameAndLocationSeparator))

            if (doesNameAndLocationSeparatorExist) {
              val restaurantName = items.headOption.flatMap(_.split(escape(rule.restaurantNameAndLocationSeparator), 2).headOption).getOrElse(NoRestaurantName)
              val approximateLocation = items.headOption.flatMap(_.split(escape(rule.restaurantNameAndLocationSeparator), 2).lastOption).getOrElse(NoApproximateLocation)
              (RestaurantName(restaurantName.trim), ApproximateLocation(approximateLocation.trim))

            } else {
              val restaurantName = items.headOption.getOrElse(NoRestaurantName)
              (RestaurantName(restaurantName.trim), ApproximateLocation(NoApproximateLocation))
            }

        case None =>
          // we can't distinguish between the text we need and the junk so we can't parse with any confidence.
          (RestaurantName(NoRestaurantName), ApproximateLocation(NoApproximateLocation))
      }
    }
  }

  case class PrefixRule(webTitlePrefix: String, junkTextSeparator: String, restaurantNameAndLocationSeparator: Seq[String]) extends Rule
  object PrefixRule {
    def checkPrefixMatches(webTitle: WebTitle, rule: PrefixRule): Option[PrefixRule] = if (webTitle.value.startsWith(rule.webTitlePrefix)) Some(rule) else None

    def applyRule(webTitle: WebTitle, rule: PrefixRule): (RestaurantName, ApproximateLocation) = {
      val items = webTitle.value.split(rule.junkTextSeparator, 2).toSeq

      val maybeSeparator: Option[String] = rule.restaurantNameAndLocationSeparator.collectFirst {
        case separator if items.lastOption.exists(_.contains(separator)) => separator
      }

      maybeSeparator match {
        case Some(separator) =>
          val restaurantName = items.lastOption.flatMap(_.split(separator, 2).headOption).getOrElse(NoRestaurantName)
          val approximateLocation = items.lastOption.flatMap(_.split(separator, 2).lastOption).getOrElse(NoRestaurantName)
          (RestaurantName(restaurantName.trim), ApproximateLocation(approximateLocation.trim))

        case None =>
          val restaurantName = items.lastOption.getOrElse(NoRestaurantName)
          (RestaurantName(restaurantName.trim), ApproximateLocation(NoApproximateLocation))
      }
    }

  }

}
