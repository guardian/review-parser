package com.gu.restaurant_review_parser.geocoding

import com.google.maps.model.{ComponentFilter, GeocodingResult}
import com.google.maps.{GeoApiContext, GeocodingApi}

object Geocoder {

  def geocode(context: GeoApiContext): String => Array[GeocodingResult] = {
    GeocodingApi.geocode(context, _: String).components(ComponentFilter.country("UK")).await()
  }

}
