package com.example.onelook.tasks.presentation.home

import android.widget.ImageView
import com.example.onelook.R
import com.example.onelook.tasks.doamin.model.DomainActivity
import com.example.onelook.tasks.doamin.model.Supplement
import com.example.onelook.tasks.presentation.home.views.CustomActivityProgressView
import kotlin.time.Duration

fun ImageView.supplementIcon(form: Supplement.Form) {
    setImageResource(
        when (form) {
            Supplement.Form.PILL -> R.drawable.ic_supplement_pill
            Supplement.Form.TABLET -> R.drawable.ic_supplement_tablet
            Supplement.Form.SACHET -> R.drawable.ic_supplement_sachet
            Supplement.Form.DROPS -> R.drawable.ic_supplement_drop
            Supplement.Form.SPOON -> R.drawable.ic_supplement_spoon
        }
    )
}

fun ImageView.activityIcon(type: DomainActivity.ActivityType) {
    setImageResource(
        when (type) {
            DomainActivity.ActivityType.RUNNING -> R.drawable.ic_activity_running
            DomainActivity.ActivityType.WALKING -> R.drawable.ic_activity_walking
            DomainActivity.ActivityType.FITNESS -> R.drawable.ic_activity_fitness
            DomainActivity.ActivityType.YOGA -> R.drawable.ic_activity_yoga
            DomainActivity.ActivityType.BREATHING -> R.drawable.ic_activity_breath
            DomainActivity.ActivityType.ROLLERSKATING -> R.drawable.ic_activity_rollers
        }
    )
}

fun CustomActivityProgressView.activityHistoryProgress(
    durationProgress: Duration,
    duration: Duration
) {
    progress = (durationProgress.inWholeSeconds / duration.inWholeSeconds.toFloat() * 100).toInt()
}