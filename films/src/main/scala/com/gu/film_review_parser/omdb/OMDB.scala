package com.gu.film_review_parser.omdb

import java.net.URLEncoder
import java.util.concurrent.TimeUnit

import com.squareup.okhttp.{OkHttpClient, Request}
import io.circe.parser._
import io.circe.generic.auto._
import cats.syntax.either._

case class OMDBData(genre: List[String],
                    year: Short,
                    imdbId: String,
                    directors: List[String],
                    actors: List[String])

object OMDB {
  private val httpClient: OkHttpClient = new OkHttpClient()
  httpClient.setConnectTimeout(60, TimeUnit.SECONDS)
  httpClient.setReadTimeout(60, TimeUnit.SECONDS)

  private def buildUrl(title: String) = {
    val enc = URLEncoder.encode(title, "UTF-8")
    s"https://omdbapi.com/?t=$enc&r=json&type=movie"
  }

  private case class OMDBResponse(Year: Short, Genre: String, Director: String, Actors: String, imdbID: String)

  def getData(title: String): Option[OMDBData] = {
    val request = new Request.Builder().url(buildUrl(title)).build
    val response = httpClient.newCall(request).execute
    if (response.isSuccessful) {
      parse(response.body.string).toOption.map(json => json.as[OMDBResponse]).flatMap {
        case Left(error) =>
          println(s"Error processing OMDB response json for title $title: $error")
          None
        case Right(responseData) =>
          val directors = List(responseData.Director)
          val actors = responseData.Actors.split(",").map(_.trim).toList
          val genres = responseData.Genre.split(",").map(_.trim).toList
          Some(OMDBData(genres, responseData.Year, responseData.imdbID, directors, actors))
      }

    } else {
      println(s"Unable to get data from OMDB for title $title. Status code was: ${response.code}, body was: ${response.body.string}")
      None
    }
  }
}
