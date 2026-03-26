package com.dontworry.app.ui.detail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri

fun openThreadLink(context: Context, link: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link)).apply {
        if (context !is Activity) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    }
}
