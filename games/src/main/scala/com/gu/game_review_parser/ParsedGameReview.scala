package com.gu.game_review_parser

import java.time.OffsetDateTime

import com.gu.contentatom.thrift.AtomData.Review
import com.gu.contentatom.thrift._
import com.gu.contentatom.thrift.atom.review._
import com.gu.contententity.thrift.Price
import com.gu.contententity.thrift.entity.game.Game

import scala.util.Try

case class ParsedGameReview(
                             contentId: String,
                             internalComposerCode: String,
                             creationDate: Option[OffsetDateTime],
                             publicationDate: OffsetDateTime,
                             reviewer: String,
                             rating: Int,
                             reviewSnippet: String,
                             title: String,
                             publisher: Option[String] = None,
                             platforms: List[String],
                             price: Option[String] = None,
                             pegiRating: Option[Int] = None,
                             genre: List[String],
                             images: Seq[Image]
                           )

object ParsedGameReview {
  val platforms = Map(
    "pc" -> "PC",
    "steam" -> "Steam",
    "ps4" -> "PS4",
    "playstation 4" -> "PS4",
    "xbox one" -> "Xbox One",
    "3ds" -> "3DS",
    "nintendo 3ds" -> "3DS",
    "vita" -> "Vita",
    "ps vita" -> "Vita",
    "mac" -> "Mac",
    "linux" -> "Linux",
    "wii u" -> "Wii U"
  )

  def toAtom(review: ParsedGameReview): Atom = {
    val gameReview = Game(
      title = review.title,
      publisher = review.publisher,
      platforms = review.platforms,
      price = review.price.flatMap(p => PriceBuilder(p)),
      pegiRating = review.pegiRating,
      genre = Nil
    )

    val contentChangeDetails = ContentChangeDetails(
      created = review.creationDate map { date =>
        ChangeRecord(
          date = date.toInstant.toEpochMilli,
          user = Some(
            User(email = "off-platform@guardian.co.uk")
          )
        )
      },
      published = Some(
        ChangeRecord(
          date = review.publicationDate.toInstant.toEpochMilli,
          user = Some(
            User(email = "off-platform@guardian.co.uk")
          )
        )
      ),
      revision = 1L)

    val rating = buildRating(review.rating)

    val reviewAtom = ReviewAtom(ReviewType.Game, review.reviewer, rating, reviewSnippet = review.reviewSnippet, entityId = "", game = Some(gameReview), sourceArticleId = Some(review.contentId), images = Some(review.images))

    Atom(
      id = generateId(review.contentId, review.title),
      atomType = AtomType.Review,
      labels = Seq.empty,
      defaultHtml = "",
      data = Review(reviewAtom),
      contentChangeDetails = contentChangeDetails
    )
  }

  private def generateId(contentId: String, title: String): String = {
    val name = contentId + title
    java.util.UUID.nameUUIDFromBytes(name.getBytes).toString
  }

  private def buildRating(rating: Int): Rating = Rating(5, rating.toShort, 0)
}

object PriceBuilder {
  private val pricePattern = """^(£|$|€)(\d+)([,.]\d\d)?$""".r

  private val currencyMap = Map(
    "£" -> "GBP",
    "$" -> "USD",
    "€" -> "EUR"
  )

  def apply(price: String): Option[Price] = {
    price match {
      /**
        * Note - an optional group that does not match returns a null value, so here maybeMinor
        * may be a null or a String.
        */
      case pricePattern(currencyChar, major, maybeMinor) =>
        val minor = Option(maybeMinor).map(_.replace(",",".")).getOrElse("")
        for {
          //Store the value in the currency's minor form
          value <- Try((major + minor).toDouble * 100).toOption
          currency <- currencyMap.get(currencyChar)
        } yield Price(currency, value.toInt)
      case _ => None
    }
  }
}
