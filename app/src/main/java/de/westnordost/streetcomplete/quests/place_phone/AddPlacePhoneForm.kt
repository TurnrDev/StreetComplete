package de.westnordost.streetcomplete.quests.place_phone

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.util.TextChangedWatcher
import kotlinx.android.synthetic.main.quest_placephone.*


class AddPlacePhoneForm : AbstractQuestFormAnswerFragment<PlacePhoneAnswer>() {

    override val contentLayoutResId = R.layout.quest_placephone

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_generic_answer_noSign) { confirmNoPhone() }
    )

    private val placePhone get() = phoneInput?.text?.toString().orEmpty().trim()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        phoneInput.addTextChangedListener(TextChangedWatcher { checkIsFormComplete() })
    }

    override fun onClickOk() {
        applyAnswer(PlacePhone(placePhone))
    }

    private fun confirmNoPhone() {
        val ctx = context ?: return
        AlertDialog.Builder(ctx)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(NoPlacePhoneSign) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    override fun isFormComplete() = placePhone.isNotEmpty()
}
