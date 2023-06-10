package com.example.onelook.ui.home

import android.widget.ImageView
import com.example.onelook.R
import com.example.onelook.data.domain.ActivityHistory
import com.example.onelook.data.domain.SupplementHistory
import com.example.onelook.views.CustomActivityProgressView
import kotlin.time.Duration

fun ImageView.supplementHistoryImage(form: SupplementHistory.Form) {
    setImageResource(
        when (form) {
            SupplementHistory.Form.PILL -> R.drawable.ic_supplement_pill
            SupplementHistory.Form.TABLET -> R.drawable.ic_supplement_tablet
            SupplementHistory.Form.SACHET -> R.drawable.ic_supplement_sachet
            SupplementHistory.Form.DROPS -> R.drawable.ic_supplement_drop
            SupplementHistory.Form.SPOON -> R.drawable.ic_supplement_spoon
        }
    )
}

fun ImageView.activityHistoryImage(type: ActivityHistory.Type) {
    setImageResource(
        when (type) {
            ActivityHistory.Type.RUNNING -> R.drawable.ic_activity_running
            ActivityHistory.Type.WALKING -> R.drawable.ic_activity_walking
            ActivityHistory.Type.FITNESS -> R.drawable.ic_activity_fitness
            ActivityHistory.Type.YOGA -> R.drawable.ic_activity_yoga
            ActivityHistory.Type.BREATHING -> R.drawable.ic_activity_breath
            ActivityHistory.Type.ROLLERSKATING -> R.drawable.ic_activity_rollers
        }
    )
}

fun CustomActivityProgressView.activityHistoryProgress(
    durationProgress: Duration,
    duration: Duration
) {
    progress = (durationProgress.inWholeSeconds / duration.inWholeSeconds * 100).toInt()
}