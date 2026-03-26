package com.dontworry.app.ui.detail

import android.content.Context
import android.content.Intent
import android.net.Uri

fun openThreadLink(context: Context, link: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
    context.startActivity(intent)
}
