package com.gu.film_review_parser

import java.time.OffsetDateTime

case class ParsedFilmReview(contentId: String,
                            internalComposerCode: String,
                            creationDate: Option[OffsetDateTime],
                            publicationDate: OffsetDateTime,
                            reviewer: String,
                            rating: Int,
                            reviewSnippet: String,
                            title: String,
                            genre: String,
                            year: String,
                            imdbId: String,
                            directors: List[String],
                            actors: List[String]
                           )

object ParsedFilmReview {

}
