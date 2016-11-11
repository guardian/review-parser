package com.gu.restaurant_review_parser.parsers.geocoding

import com.google.maps.model._


object TestGeocodingResultCreator {

  def geocodingResult(formattedAddress: String, lat: Double, lng: Double, addressComponents: Array[AddressComponent]): GeocodingResult = {
    val geocodingResult = new GeocodingResult
    geocodingResult.formattedAddress = formattedAddress
    geocodingResult.addressComponents = addressComponents
    geocodingResult.geometry = geometry(lat, lng)
    geocodingResult
  }

  def addressComponent(longName: String, shortName: String, types: Array[AddressComponentType]): AddressComponent = {
    val addressComponent = new AddressComponent
    addressComponent.longName = longName
    addressComponent.shortName = shortName
    addressComponent.types = types
    addressComponent
  }

  private def geometry(lat: Double, lng: Double): Geometry = {
    val geometry = new Geometry
    geometry.location = new LatLng(51.5390429, -0.1026274)
    geometry
  }

}
