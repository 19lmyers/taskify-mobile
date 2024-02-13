package dev.chara.taskify.shared.domain

import android.content.Context
import android.content.Intent

class AndroidShareSheetManager(private val context: Context) : ShareSheetManager {
    override fun shareList(token: String) {
        val message = "I've shared a workspace with you! Join at: "
        val url = "https://taskify.chara.dev/join?token=$token"

        val text = message + url
        val html = "$message <a href='$url'>$url</a>"

        val sendIntent: Intent =
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                putExtra(Intent.EXTRA_HTML_TEXT, html)
                type = "text/plain"
            }

        val shareIntent = Intent.createChooser(sendIntent, null)

        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // TODO is this really what I want?

        context.startActivity(shareIntent)
    }
}