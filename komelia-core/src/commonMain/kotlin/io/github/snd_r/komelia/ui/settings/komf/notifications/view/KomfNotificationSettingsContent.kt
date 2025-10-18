package io.github.snd_r.komelia.ui.settings.komf.notifications.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.common.StateHolder
import io.github.snd_r.komelia.ui.settings.komf.notifications.AppriseState
import io.github.snd_r.komelia.ui.settings.komf.notifications.DiscordState

@Composable
fun KomfSettingsContent(
    discordState: DiscordState,
    appriseState: AppriseState,
) {
    Column {
        var selectedTab by remember { mutableStateOf(0) }
        SecondaryTabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier.heightIn(min = 40.dp).cursorForHand(),
            ) {
                Text("Discord")
            }
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier.heightIn(min = 40.dp).cursorForHand(),
            ) {
                Text("Apprise")
            }
        }
        Spacer(Modifier.height(30.dp))

        when (selectedTab) {
            0 -> DiscordNotificationsContent(
                discordUploadSeriesCover = StateHolder(
                    discordState.discordUploadSeriesCover,
                    discordState::onSeriesCoverChange
                ),
                discordWebhooks = discordState.discordWebhooks,
                onDiscordWebhookAdd = discordState::onDiscordWebhookAdd,
                onDiscordWebhookRemove = discordState::onDiscordWebhookRemove,

                titleTemplate = StateHolder(discordState.titleTemplate, discordState::titleTemplate::set),
                titleUrlTemplate = StateHolder(discordState.titleUrlTemplate, discordState::onTitleUrlTemplateChange),
                descriptionTemplate = StateHolder(
                    discordState.descriptionTemplate,
                    discordState::descriptionTemplate::set
                ),
                fieldTemplates = discordState.fieldTemplates,
                onFieldAdd = discordState::onFieldAdd,
                onFieldDelete = discordState::onFieldDelete,
                footerTemplate = StateHolder(discordState.footerTemplate, discordState::footerTemplate::set),

                titlePreview = discordState.titlePreview,
                titleUrlPreview = discordState.titleUrlPreview,
                descriptionPreview = discordState.descriptionPreview,
                fieldPreviews = discordState.fieldPreviews,
                footerPreview = discordState.footerPreview,

                notificationContextState = discordState.notificationContext,
                onTemplateSave = discordState::onTemplatesSave,
                onTemplateSend = discordState::onTemplatesSend,
                onTemplateRender = discordState::onTemplateRender,
            )

            else -> AppriseContent(
                urls = appriseState.appriseUrls,
                onUrlAdd = appriseState::onUrlAdd,
                onUrlRemove = appriseState::onUrlRemove,
                uploadSeriesCover = appriseState.uploadSeriesCover,
                onUploadSeriesCoverChange = appriseState::onSeriesCoverChange,
                titleTemplate = appriseState.titleTemplate,
                onTitleTemplateChange = appriseState::titleTemplate::set,
                bodyTemplate = appriseState.bodyTemplate,
                onBodyTemplateChange = appriseState::bodyTemplate::set,
                notificationContextState = appriseState.notificationContext,
                onTemplateSend = appriseState::onTemplatesSend,
                onTemplateSave = appriseState::onTemplatesSave
            )
        }


    }
}

