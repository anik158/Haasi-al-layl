package com.example.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager

/** The developer's website and feedback address, and the two intents that use them. */
object DeveloperContact {

  const val DEVELOPER_NAME = "Ahsanul Houqe"
  const val WEBSITE = "https://www.ahsanulhouqe.com/"
  const val FEEDBACK_EMAIL = "ahsanan@outlook.com"

  /**
   * Where the privacy policy is published. The text is `docs/privacy-policy.html` in this repo;
   * it has to be uploaded to exactly this address before the Play listing goes live.
   */
  const val PRIVACY_POLICY = "https://www.ahsanulhouqe.com/haasi-al-layl/privacy/"

  fun openPrivacyPolicy(context: Context) = openUrl(context, PRIVACY_POLICY)

  fun openWebsite(context: Context) = openUrl(context, WEBSITE)

  private fun openUrl(context: Context, url: String) {
    try {
      context.startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      )
    } catch (_: ActivityNotFoundException) {
      Toast.makeText(context, url, Toast.LENGTH_LONG).show()
    }
  }

  /**
   * Opens the default mail app addressed to [FEEDBACK_EMAIL], with the app version and Android
   * release in the subject so a report says what it was seen on.
   *
   * `ACTION_SENDTO` with a `mailto:` URI, so only mail apps answer it. Not gated on
   * resolveActivity, which returns null on Android 11+ without a `<queries>` entry; a missing mail
   * app is caught instead, and the address shown so it can still be written to.
   */
  fun sendFeedback(context: Context, language: AppLanguage) {
    val version = runCatching {
      context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull() ?: "?"
    val subject = "${LocalizationManager.getAppName(AppLanguage.ENGLISH)} feedback " +
      "(v$version, Android ${Build.VERSION.RELEASE})"
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$FEEDBACK_EMAIL")).apply {
      putExtra(Intent.EXTRA_EMAIL, arrayOf(FEEDBACK_EMAIL))
      putExtra(Intent.EXTRA_SUBJECT, subject)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
      context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
      Toast.makeText(
        context,
        LocalizationManager.getNoMailAppMessage(FEEDBACK_EMAIL, language),
        Toast.LENGTH_LONG
      ).show()
    }
  }
}
