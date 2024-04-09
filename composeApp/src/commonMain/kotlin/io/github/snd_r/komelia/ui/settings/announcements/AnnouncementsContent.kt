package io.github.snd_r.komelia.ui.settings.announcements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komga.announcements.KomgaJsonFeed.KomgaAnnouncement

@Composable
fun AnnouncementsContent(announcements: List<KomgaAnnouncement>) {
    Column(verticalArrangement = Arrangement.spacedBy(50.dp)) {
        announcements.forEach {
            Announcement(it)
            HorizontalDivider()
        }
    }
}

@OptIn(ExperimentalRichTextApi::class)
@Composable
private fun Announcement(announcement: KomgaAnnouncement) {
    Column {
        announcement.title?.let { title ->
            AnnouncementTitle(title, announcement.url)
        }

        announcement.dateModified?.let {
            Text(it.toString(), modifier = Modifier.padding(vertical = 20.dp))
        }

        announcement.contentHtml?.let {
            SelectionContainer {
                val state = rememberRichTextState()
                state.setConfig(
                    linkColor = MaterialTheme.colorScheme.secondary,
                    linkTextDecoration = TextDecoration.Underline,
                    codeBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    codeStrokeColor = MaterialTheme.colorScheme.surfaceVariant
                )
                state.setHtml(it)
                RichText(state)
            }
        }
    }
}

@Composable
private fun AnnouncementTitle(title: String, url: String?) {
    val uriHandler = LocalUriHandler.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered = interactionSource.collectIsHoveredAsState()

    val onClickModifier = url?.let {
        Modifier
            .clickable(interactionSource = interactionSource, indication = null) { uriHandler.openUri(it) }
            .hoverable(interactionSource)
            .cursorForHand()
    } ?: Modifier

    val style =
        if (isHovered.value)
            MaterialTheme.typography.headlineLarge.copy(textDecoration = TextDecoration.Underline)
        else
            MaterialTheme.typography.headlineLarge

    Text(title, style = style, modifier = onClickModifier)
}
