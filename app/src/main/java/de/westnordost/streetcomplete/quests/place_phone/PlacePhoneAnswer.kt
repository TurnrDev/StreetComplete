package de.westnordost.streetcomplete.quests.place_phone

sealed class PlacePhoneAnswer

data class PlacePhone(val phone: String) : PlacePhoneAnswer()
object NoPlacePhoneSign : PlacePhoneAnswer()
