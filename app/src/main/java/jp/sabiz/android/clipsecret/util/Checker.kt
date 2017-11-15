package jp.sabiz.android.clipsecret.util

import android.content.Intent
import android.net.Uri
import android.webkit.URLUtil

/**
 * @auther sabi
 * Created on 2017/11/12.
 */

fun getUrlFromIntent(intent: Intent):CharSequence {
    if (Intent.ACTION_SEND != intent.action) {
        return ""
    }
    val urlString = intent.extras.getCharSequence(Intent.EXTRA_TEXT).toString()
    if(!URLUtil.isValidUrl(urlString)){
        return ""
    }
    val url = Uri.parse(urlString)
    return url.scheme+ "://" + url.host
}

fun getTitleFromIntent(intent: Intent):CharSequence {
    if (Intent.ACTION_SEND != intent.action) {
        return ""
    }
    return intent.extras.getCharSequence(Intent.EXTRA_SUBJECT)
}