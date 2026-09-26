package snd.komelia.ui.series.mangabaka

import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import snd.komelia.image.coil.KomfFaviconRequest
import snd.komelia.ui.series.mangabaka.state.MangaBakaLinksState
import snd.komf.api.mangabaka.MangaBakaLink

@Composable
fun MangaBakaLinks(state: MangaBakaLinksState) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(30.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            for (group in state.linkGroups) {
                LinkGroup(group.name, group)
            }
        }
    }
}

@Composable
private fun LinkGroup(groupName: String, group: MangaBakaLinksState.LinkGroup) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            groupName,
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.labelLargeEmphasized,
            fontWeight = FontWeight.Bold
        )
        ColorSpacerContent {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                maxItemsInEachRow = 3
            ) {
                for (subgroup in group.subgroups) {
                    LinkSubGroup(subgroup)
                }
                if (group.hiddenLinks.isNotEmpty()) {
                    LinksHidden(group.hiddenLinks)
                }
            }
        }
    }
}

@Composable
private fun LinkSubGroup(subgroup: MangaBakaLinksState.LinkSubGroup) {
    if (subgroup.links.size == 1) {
        val link = subgroup.links.first()
        UrlLinkChip(
            url = link.url,
            label = link.nameDisplay,
            faviconUrl = link.url
        )
    } else {
        val state = rememberTooltipState(
            isPersistent = true,
            mutatorMutex = LinkTooltipMutatorMutex.linkSubgroupMutatorMutex
        )
        val coroutineScope = rememberCoroutineScope()
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below, 5.dp),
            tooltip = {
                RichTooltip(
                    title = { Text(subgroup.name) },
                    maxWidth = 400.dp
                ) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        for (link in subgroup.links) {
                            TooltipBox(
                                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                                state = rememberTooltipState(mutatorMutex = LinkTooltipMutatorMutex.linkUrlMutatorMutex),
                                tooltip = { PlainTooltip(maxWidth = 300.dp) { Text(link.url) } }
                            ) {
                                if (link.name == "x.com") {
                                    UrlLinkChip(
                                        url = link.url,
                                        label = "@${link.url.removePrefix("https://x.com/")}",
                                        faviconUrl = link.url
                                    )
                                } else {
                                    UrlLinkChip(
                                        url = link.url,
                                        label = link.language,
                                    )
                                }
                            }
                        }
                    }
                }
            },
            state = state,
            enableUserInput = false
        ) {
            LinkChip(
                onClick = {
                    coroutineScope.launch {
                        if (state.isVisible) state.dismiss()
                        else state.show()
                    }
                },
                label = "${subgroup.name} (${subgroup.links.size})",
                faviconUrl = subgroup.links.first().url
            )
        }
    }
}

@Composable
private fun LinksHidden(links: List<MangaBakaLink>) {
    val state = rememberTooltipState(
        isPersistent = true,
        mutatorMutex = LinkTooltipMutatorMutex.linkSubgroupMutatorMutex
    )
    val coroutineScope = rememberCoroutineScope()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below, 5.dp),
        tooltip = {
            RichTooltip(
                title = { Text("OTHER LINKS") },
                maxWidth = 400.dp
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    for (link in links) {
                        UrlLinkChip(
                            url = link.url,
                            label = link.nameDisplay,
                            faviconUrl = link.url
                        )

                    }
                }
            }
        },
        state = state,
        enableUserInput = false
    ) {
        LinkChip(
            onClick = {
                coroutineScope.launch {
                    if (state.isVisible) state.dismiss()
                    else state.show()
                }
            },
            label = "${links.size} hidden",
        )
    }
}

@Composable
private fun LinkChip(
    onClick: () -> Unit,
    label: String,
    faviconUrl: String? = null,
) {
    FilterChip(
        selected = false,
        onClick = onClick,
        leadingIcon = faviconUrl?.let { url ->
            {
                AsyncImage(
                    model = KomfFaviconRequest(url),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
    )
}

@Composable
private fun UrlLinkChip(
    url: String,
    label: String,
    faviconUrl: String? = null,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        state = rememberTooltipState(mutatorMutex = LinkTooltipMutatorMutex.linkUrlMutatorMutex),
        tooltip = { PlainTooltip(maxWidth = 300.dp) { Text(url) } }
    ) {
        val uriHandler = LocalUriHandler.current
        LinkChip(
            onClick = { uriHandler.openUri(url) },
            label = label,
            faviconUrl = faviconUrl
        )
    }
}

private object LinkTooltipMutatorMutex {
    val linkUrlMutatorMutex: MutatorMutex = MutatorMutex()
    val linkSubgroupMutatorMutex: MutatorMutex = MutatorMutex()

}