package com.gu.film_review_parser

import java.time.OffsetDateTime

import com.gu.contentatom.thrift.AtomData.Review
import com.gu.contentatom.thrift.atom.review.{Rating, ReviewAtom, ReviewType}
import com.gu.contentatom.thrift._
import com.gu.contententity.thrift.entity.film.Film
import com.gu.contententity.thrift.entity.person.Person

case class ParsedFilmReview(contentId: String,
                            internalComposerCode: String,
                            creationDate: Option[OffsetDateTime],
                            publicationDate: OffsetDateTime,
                            reviewer: String,
                            rating: Int,
                            reviewSnippet: String,
                            title: String,
                            genre: List[String],
                            year: Short,
                            imdbId: String,
                            directors: List[String],
                            actors: List[String]
                           )

object ParsedFilmReview {
  def toAtom(parsed: ParsedFilmReview): Atom = {
    val filmReview = Film(
      parsed.title,
      parsed.year,
      parsed.imdbId,
      parsed.directors.map(d => Person(fullName = d)),
      parsed.actors.map(a => Person(fullName = a)),
      parsed.genre
    )

    val contentChangeDetails = ContentChangeDetails(
      created = parsed.creationDate map { date =>
        ChangeRecord(
          date = date.toInstant.toEpochMilli,
          user = Some(
            User(email = "off-platform@guardian.co.uk")
          )
        )
      },
      published = Some(
        ChangeRecord(
          date = parsed.publicationDate.toInstant.toEpochMilli,
          user = Some(
            User(email = "off-platform@guardian.co.uk")
          )
        )
      ),
      revision = 1L)

    val rating = Rating(5, parsed.rating.toShort, 0)

    val reviewAtom = ReviewAtom(ReviewType.Film, parsed.reviewer, rating, reviewSnippet = parsed.reviewSnippet, entityId = "", film = Some(filmReview))

    Atom(
      id = generateId(parsed.contentId),
      atomType = AtomType.Review,
      labels = Seq.empty,
      defaultHtml = "",
      data = Review(reviewAtom),
      contentChangeDetails = contentChangeDetails
    )
  }

  private def generateId(contentId: String): String = java.util.UUID.nameUUIDFromBytes(contentId.getBytes).toString
}
