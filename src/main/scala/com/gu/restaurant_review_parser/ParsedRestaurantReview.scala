package com.gu.restaurant_review_parser

import java.time.OffsetDateTime

import com.google.maps.model.{AddressComponent, AddressComponentType}

case class WebAddress(value: String) extends AnyVal
case class FormattedAddress(value: String) extends AnyVal
case class RestaurantInformation(value: String) extends AnyVal
case class ArticleBody(value: String) extends AnyVal
case class RestaurantName(value: String) extends AnyVal
case class ApproximateLocation(value: String) extends AnyVal
case class WebTitle(value: String) extends AnyVal

sealed trait AddressPart
case class StreetNumber(value: String) extends AddressPart
case class Route(value: String) extends AddressPart
case class Neighborhood(value: String) extends AddressPart
case class Locality(value: String) extends AddressPart
case class PostalCode(value: String) extends AddressPart
case class PostalTown(value: String) extends AddressPart
case class Country(value: String) extends AddressPart
case class AdministrativeAreaLevelOne(value: String) extends AddressPart
case class AdministrativeAreaLevelTwo(value: String) extends AddressPart
case object Unknown extends AddressPart

case class Location(latitude: Double, longitude: Double)

case class AddressParts(streetNumber: Option[StreetNumber],
                        route: Option[Route],
                        neighborhood: Option[Neighborhood],
                        locality: Option[Locality],
                        postalCode: Option[PostalCode],
                        postalTown: Option[PostalTown],
                        country: Option[Country],
                        administrativeAreaLevelOne: Option[AdministrativeAreaLevelOne],
                        administrativeAreaLevelTwo: Option[AdministrativeAreaLevelTwo])
object AddressParts {

  /**
    * Constructs  AddressParts and makes a bet on whether the resulting parts are likely to be accurate or not based on the completeness
    * of the different component types we expect back from the Google API.
    * @param addressComponents
    * @return
    */
  def apply(addressComponents: Array[AddressComponent]): Option[AddressParts] = {
    val addressParts: Array[AddressPart] =
      for {
        addrComponent <- addressComponents
        addrCompType <- addrComponent.types
      } yield {
        addrCompType match {
          case AddressComponentType.STREET_NUMBER => StreetNumber(addrComponent.longName)
          case AddressComponentType.ROUTE => Route(addrComponent.longName)
          case AddressComponentType.NEIGHBORHOOD => Neighborhood(addrComponent.longName)
          case AddressComponentType.LOCALITY => Locality(addrComponent.longName)
          case AddressComponentType.POSTAL_CODE => PostalCode(addrComponent.longName)
          case AddressComponentType.POSTAL_TOWN => PostalTown(addrComponent.longName)
          case AddressComponentType.COUNTRY => Country(addrComponent.longName)
          case AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1 => AdministrativeAreaLevelOne(addrComponent.longName)
          case AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_2 => AdministrativeAreaLevelTwo(addrComponent.longName)
          case _ => Unknown
      }
    }

    val addrParts = AddressParts(
      streetNumber = addressParts.collectFirst { case s: StreetNumber => s },
      route = addressParts.collectFirst { case r: Route => r },
      neighborhood = addressParts.collectFirst { case n: Neighborhood => n },
      locality = addressParts.collectFirst { case l: Locality => l },
      postalCode = addressParts.collectFirst { case pc: PostalCode => pc },
      postalTown = addressParts.collectFirst { case pt: PostalTown => pt },
      country = addressParts.collectFirst { case c: Country => c },
      administrativeAreaLevelOne = addressParts.collectFirst { case a1: AdministrativeAreaLevelOne => a1 },
      administrativeAreaLevelTwo = addressParts.collectFirst { case a2: AdministrativeAreaLevelTwo => a2 }
    )

    if (addrParts.route.nonEmpty && addrParts.locality.nonEmpty)
      Some(addrParts)
    else None

  }
}
case class AddressInformation(addressParts: AddressParts, location: Location)

sealed trait RatingBreakdownType {
  val rating: String
}
case class FoodRating(rating: String) extends RatingBreakdownType
case class AtmosphereRating(rating: String) extends RatingBreakdownType
case class ValueForMoneyRating(rating: String) extends RatingBreakdownType

case class RatingBreakdown(food: FoodRating, atmosphere: AtmosphereRating, valueForMoney: ValueForMoneyRating)

case class ParsedRestaurantReview (
                                    restaurantName: RestaurantName,
                                    approximateLocation: ApproximateLocation,
                                    reviewer: String,
                                    publicationDate: OffsetDateTime,
                                    ratingBreakdown: Option[RatingBreakdown],
                                    address: Option[FormattedAddress],
                                    addressInformation: Option[AddressInformation],
                                    restaurantInformation: Option[RestaurantInformation],
                                    webAddress: Option[WebAddress]
) {

  override def toString: String = {
    s"Restaurant name: ${restaurantName.value}, \n" +
    s"Rough location: ${approximateLocation.value}, \n" +
    s"Reviewer: $reviewer, \n" +
    s"Publication date: ${publicationDate.toString}, \n" +
    s"Ratings: ${ratingBreakdown.getOrElse(ParsedRestaurantReview.NoRatingBreakdown)}, \n" +
    s"Address: ${address.getOrElse(ParsedRestaurantReview.NoAddress)}, \n" +
    s"Address Information: ${addressInformation.getOrElse(ParsedRestaurantReview.NoAddressInformation)}, \n" +
    s"Restaurant information: ${restaurantInformation.getOrElse(ParsedRestaurantReview.NoRestaurantInformation)}, \n" +
    s"Web address: ${webAddress.getOrElse(ParsedRestaurantReview.NoWebAddress)} \n"
  }
}

object ParsedRestaurantReview {
  val NoRestaurantName = "NO RESTAURANT NAME"
  val NoApproximateLocation = "NO APPROXIMATE LOCATION"
  val NoRatingBreakdown = "NO RATING BREAKDOWN"
  val NoAddress = "NO ADDRESS"
  val NoRestaurantInformation = "NO RESTAURANT INFORMATION"
  val NoAddressInformation = "NO ADDRESS INFORMATION"
  val NoWebAddress = "NO WEB ADDRESS"
}

