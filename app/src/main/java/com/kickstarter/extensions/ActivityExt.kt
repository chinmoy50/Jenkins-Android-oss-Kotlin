package com.kickstarter.extensions

import android.app.Activity
import android.content.Intent
import com.kickstarter.R

fun Activity.startActivityWithSlideUpTransition(intent: Intent) {
    this.startActivity(intent)
    this.overridePendingTransition(R.anim.settings_bottom_slide, R.anim.fade_out)
}