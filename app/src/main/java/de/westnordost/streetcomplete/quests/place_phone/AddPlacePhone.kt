package de.westnordost.streetcomplete.quests.place_phone

import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.ktx.containsAny
import java.util.concurrent.FutureTask

class AddPlacePhone(
    private val featureDictionaryFuture: FutureTask<FeatureDictionary>
) : OsmElementQuestType<PlacePhoneAnswer> {

    private val filter by lazy { ("""
        nodes, ways, relations with
        (
          shop and shop !~ no|vacant
          or craft
          or office
          or tourism = information and information = office
          or """.trimIndent() +

        // The common list is shared by the name quest, the opening hours quest and the wheelchair quest.
        // So when adding other tags to the common list keep in mind that they need to be appropriate for all those quests.
        // Independent tags can by added in the "name only" tab.

        mapOf(
            "amenity" to arrayOf(
                // common
                "restaurant", "cafe", "ice_cream", "fast_food", "food_court",                                          // eat & drink
                "cinema", "planetarium", "casino",                                                                     // amenities
                "bank", "bureau_de_change", "money_transfer", "post_office", "marketplace", "internet_cafe",           // commercial
                "car_wash", "car_rental", "fuel",                                                                      // car stuff
                "dentist", "doctors", "clinic", "pharmacy", "veterinary",                                              // health
                "animal_boarding", "animal_shelter", "animal_breeding",                                                // animals

                // name & opening hours
                "boat_rental",

                // name & wheelchair
                "theatre",                             // culture
                "conference_centre", "arts_centre",    // events
                "police", "ranger_station",            // civic
                "ferry_terminal",                      // transport
                "place_of_worship",                    // religious
                "hospital",                            // health care

                // name only
                "studio",                                                                // culture
                "events_venue", "exhibition_centre", "music_venue",                      // events
                "prison", "fire_station",                                                // civic
                "social_facility", "nursing_home", "childcare", "retirement_home", "social_centre", // social
                "monastery",                                                             // religious
                "kindergarten", "school", "college", "university", "research_institute", // education
                "driving_school", "dive_centre", "language_school", "music_school",      // learning
                "brothel", "gambling", "love_hotel", "stripclub"                         // bad stuff
            ),
            "tourism" to arrayOf(
                // common
                "zoo", "aquarium", "theme_park", "gallery", "museum",

                // name & wheelchair
                "attraction",
                "hotel", "guest_house", "motel", "hostel", "alpine_hut", "apartment", "resort", "camp_site", "caravan_site", "chalet" // accommodations

                // and tourism = information, see above
            ),
            "leisure" to arrayOf(
                // common
                "fitness_centre", "golf_course", "water_park", "miniature_golf", "bowling_alley",
                "amusement_arcade", "adult_gaming_centre", "tanning_salon",

                // name & wheelchair
                "sports_centre", "stadium", "marina",

                // name & opening hours
                "horse_riding",

                // name only
                "dance", "nature_reserve"
            ),
        ).map { it.key + " ~ " + it.value.joinToString("|") }.joinToString("\n  or ") + "\n" + """
        )
        and !phone and !contact:phone and phone:signed != no
    """.trimIndent()).toElementFilterExpression() }

    override val commitMessage = "Determine phone number for places (Private SC quest)"
    override val wikiLink = "Key:phone"
    override val icon = R.drawable.ic_quest_phone
    override val isReplaceShopEnabled = true

    override fun getTitle(tags: Map<String, String>): Int {
        val hasProperName = hasProperName(tags)
        val hasFeatureName = hasFeatureName(tags)
        return when {
            !hasProperName  -> R.string.quest_placePhone_no_name_title
            !hasFeatureName -> R.string.quest_placePhone_name_title
            else            -> R.string.quest_placePhone_name_type_title
        }
    }

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val name = tags["name"] ?: tags["brand"]
        val hasProperName = name != null
        val hasFeatureName = hasFeatureName(tags)
        return when {
            !hasProperName  -> arrayOf(featureName.value.toString())
            !hasFeatureName -> arrayOf(name!!)
            else            -> arrayOf(name!!, featureName.value.toString())
        }
    }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element) : Boolean {
        if (!filter.matches(element)) return false
        if (!hasName(element.tags)) return false
        return filter.matches(element)
    }

    override fun createForm() = AddPlacePhoneForm()

    override fun applyAnswerTo(answer: PlacePhoneAnswer, changes: StringMapChangesBuilder) {
        when (answer) {
            is NoPlacePhoneSign -> changes.add("phone:signed", "no")
            is PlacePhone -> changes.add("phone", answer.phone)
        }
    }

    private fun hasName(tags: Map<String, String>) = hasProperName(tags) || hasFeatureName(tags)

    private fun hasProperName(tags: Map<String, String>): Boolean =
        tags.keys.containsAny(listOf("name", "brand"))

    private fun hasFeatureName(tags: Map<String, String>): Boolean =
        featureDictionaryFuture.get().byTags(tags).isSuggestion(false).find().isNotEmpty()
    }
