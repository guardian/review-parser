package com.gu.restaurant_review_parser.parsers

import com.gu.restaurant_review_parser.{ApproximateLocation, RestaurantName, WebTitle}

object Rules {

  sealed trait Rule
  case class SuffixRule(webTitleSuffix: String, junkTextSeparator: Seq[String], restaurantNameAndLocationSeparator: String) extends Rule
  object SuffixRule {
    def checkSuffixMatches(webTitle: WebTitle, rule: SuffixRule): Option[SuffixRule] = if (webTitle.value.endsWith(rule.webTitleSuffix)) Some(rule) else None

    def applyRule(webTitle: WebTitle, rule: SuffixRule): (Option[RestaurantName], Option[ApproximateLocation]) = {

      def escape(sep: String): String = if (sep == " | ") "\\|" else sep

      val maybeJunkTextSeparator: Option[String] = rule.junkTextSeparator.collectFirst { case sep if webTitle.value.contains(sep) => sep }

      maybeJunkTextSeparator match {
        case Some(separator) =>
            val items = webTitle.value.split(escape(separator), 2).toSeq
            val doesNameAndLocationSeparatorExist = items.headOption.exists(_.contains(rule.restaurantNameAndLocationSeparator))

            if (doesNameAndLocationSeparatorExist) {
              val maybeRestaurantName = items.headOption.flatMap(_.split(escape(rule.restaurantNameAndLocationSeparator), 2).headOption).map(name => RestaurantName(name.trim))
              val maybeApproximateLocation = items.headOption.flatMap(_.split(escape(rule.restaurantNameAndLocationSeparator), 2).lastOption).map(location => ApproximateLocation(location.trim))
              (maybeRestaurantName, maybeApproximateLocation)

            } else {
              val maybeRestaurantName = items.headOption.map(name => RestaurantName(name.trim))
              (maybeRestaurantName, None)
            }

        case None =>
          // we can't distinguish between the text we need and the junk so we can't parse with any confidence.
          (None, None)
      }
    }
  }

  case class PrefixRule(webTitlePrefix: String, junkTextSeparator: String, restaurantNameAndLocationSeparator: Seq[String]) extends Rule
  object PrefixRule {
    def checkPrefixMatches(webTitle: WebTitle, rule: PrefixRule): Option[PrefixRule] = if (webTitle.value.startsWith(rule.webTitlePrefix)) Some(rule) else None

    def applyRule(webTitle: WebTitle, rule: PrefixRule): (Option[RestaurantName], Option[ApproximateLocation]) = {
      val items = webTitle.value.split(rule.junkTextSeparator, 2).toSeq

      val maybeSeparator: Option[String] = rule.restaurantNameAndLocationSeparator.collectFirst {
        case separator if items.lastOption.exists(_.contains(separator)) => separator
      }

      maybeSeparator match {
        case Some(separator) =>
          val maybeRestaurantName = items.lastOption.flatMap(_.split(separator, 2).headOption).map(name => RestaurantName(name.trim))
          val maybeApproximateLocation = items.lastOption.flatMap(_.split(separator, 2).lastOption).map(location => ApproximateLocation(location.trim))
          (maybeRestaurantName, maybeApproximateLocation)

        case None =>
          val maybeRestaurantName = items.lastOption.map(name => RestaurantName(name.trim))
          (maybeRestaurantName, None)
      }
    }

  }

}
