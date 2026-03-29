package com.dontworry.app.ui.detail

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

fun openThreadLink(context: Context, link: String) {
    val uri = Uri.parse(link)
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
        if (context !is Activity) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    val chooser = Intent.createChooser(intent, null).apply {
        if (context !is Activity) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    try {
        context.startActivity(chooser)
    } catch (_: ActivityNotFoundException) {
        // No browser installed or available to handle the link.
    }
}
