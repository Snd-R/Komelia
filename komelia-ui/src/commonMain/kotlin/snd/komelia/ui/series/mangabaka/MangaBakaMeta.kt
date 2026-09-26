package snd.komelia.ui.series.mangabaka

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.animeNewsNetwork
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.mangabaka
import org.jetbrains.compose.resources.painterResource
import snd.komelia.ui.icons.AppIcons
import snd.komelia.ui.icons.mangabaka.AniList
import snd.komelia.ui.icons.mangabaka.AnimePlanet
import snd.komelia.ui.icons.mangabaka.Kitsu
import snd.komelia.ui.icons.mangabaka.MangaUpdates
import snd.komelia.ui.icons.mangabaka.MyAnimeList
import snd.komelia.ui.icons.mangabaka.Shikimori
import snd.komelia.ui.icons.mangabaka.flags.FlagUS
import snd.komelia.ui.icons.outlined.DesignServices
import snd.komelia.ui.icons.outlined.Edit
import snd.komelia.ui.icons.outlined.EuroSymbol
import snd.komelia.ui.icons.outlined.LiveTv
import snd.komelia.ui.icons.outlined.NewsStand
import snd.komelia.ui.icons.outlined.PlayArrow
import snd.komelia.ui.icons.outlined.Toc
import snd.komelia.ui.series.mangabaka.state.MangaBakaMetadataState
import snd.komf.api.mangabaka.KomfMangaBakaSeries
import snd.komf.api.mangabaka.MangaBakaStatus
import kotlin.math.roundToInt

@Composable
fun MangaBakaMeta(mangaBakaState: MangaBakaMetadataState) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
            GeneralMeta(mangaBakaState)
            Authors(mangaBakaState)
            Publishers(mangaBakaState)
            AnimeInfo(mangaBakaState.metadata)
            SourcesAndScores(mangaBakaState.metadata)
        }
    }
}

@Composable
private fun GeneralMeta(mangaBakaState: MangaBakaMetadataState) {
    val chipColors = FilterChipDefaults.filterChipColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
    val chipPadding = PaddingValues(horizontal = 5.dp)
    val chipModifier = Modifier//.height(28.dp)
    val metadata = mangaBakaState.metadata
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        FilterChip(
            modifier = chipModifier,
            selected = false,
            onClick = {},
            contentPadding = chipPadding,
            colors = chipColors,
            label = { Text(metadata.type.name, style = MaterialTheme.typography.labelMedium) }
        )

        val (statusColor, statusIcon) =
            when (metadata.status) {
                MangaBakaStatus.CANCELLED -> FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(.5f),
                    labelColor = MaterialTheme.colorScheme.onErrorContainer
                ) to null

                MangaBakaStatus.COMPLETED -> FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(.5f),
                    labelColor = MaterialTheme.colorScheme.onSecondary
                ) to null

                MangaBakaStatus.HIATUS -> FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(.5f),
                    labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) to null

                MangaBakaStatus.RELEASING -> chipColors to AppIcons.Outlined.PlayArrow
                MangaBakaStatus.UPCOMING -> chipColors to null
                MangaBakaStatus.UNKNOWN -> chipColors to null
            }

        FilterChip(
            modifier = chipModifier,
            selected = false,
            onClick = {},
            contentPadding = chipPadding,
            leadingIcon = { if (statusIcon != null) Icon(statusIcon, null) },
            colors = statusColor,

            label = { Text(metadata.status.name, style = MaterialTheme.typography.labelMedium) },
        )
        if (metadata.isLicensed) {
            FilterChip(
                modifier = chipModifier,
                selected = false,
                onClick = {},
                colors = chipColors,
                contentPadding = chipPadding,
                leadingIcon = { Icon(AppIcons.Outlined.EuroSymbol, null) },
                label = { Text("Licensed", style = MaterialTheme.typography.labelMedium) }
            )

        }

        metadata.finalVolume?.let { finalVolume ->
            FilterChip(
                modifier = chipModifier,
                selected = false,
                onClick = {},
                colors = chipColors,
                contentPadding = chipPadding,
                leadingIcon = { Icon(AppIcons.Outlined.NewsStand, null) },
                label = { Text("$finalVolume Vol.", style = MaterialTheme.typography.labelMedium) }
            )
        }

        metadata.totalChapters?.let { totalChapters ->
            FilterChip(
                modifier = chipModifier,
                selected = false,
                onClick = {},
                colors = chipColors,
                contentPadding = chipPadding,
                leadingIcon = { Icon(AppIcons.Outlined.Toc, null) },
                label = { Text("$totalChapters Ch.", style = MaterialTheme.typography.labelMedium) }
            )
        }
        metadata.published?.let { published ->
            val startDate = published.startDate ?: return@let

            FilterChip(
                modifier = chipModifier,
                selected = false,
                onClick = {},
                colors = chipColors,
                contentPadding = chipPadding,
                label = {
                    Text(buildString {
                        append(startDate.year)
                        published.endDate?.let { append("-${it.year}") }
                    }, style = MaterialTheme.typography.labelMedium)
                })
        }

        metadata.anime?.let {
            FilterChip(
                modifier = chipModifier,
                selected = false,
                onClick = {},
                colors = chipColors,
                leadingIcon = { Icon(AppIcons.Outlined.LiveTv, null) },
                contentPadding = chipPadding,
                label = { Text("Anime", style = MaterialTheme.typography.labelMedium) }
            )
        }

    }
}

@Composable
private fun Authors(
    mangaBakaState: MangaBakaMetadataState
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        val (artists, authors) = remember(mangaBakaState.metadata) {
            val artists = mangaBakaState.metadata.artists?.toSet() ?: emptySet()
            val authors = (mangaBakaState.metadata.authors?.toSet() ?: emptySet()).subtract(artists)
            artists.take(5) to authors.take(5)
        }

        for (artist in artists) {
            SuggestionChip(
                onClick = {},
                label = {
                    Icon(AppIcons.Outlined.DesignServices, null)
                    Text(artist, style = MaterialTheme.typography.labelMedium)
                },
                contentPadding = PaddingValues.Zero
            )

        }
        for (author in authors) {
            SuggestionChip(
                onClick = {},
                label = {
                    Icon(AppIcons.Outlined.Edit, null)
                    Text(author, style = MaterialTheme.typography.labelMedium)
                },
                contentPadding = PaddingValues.Zero
            )
        }

    }
}

@Composable
private fun Publishers(mangaBakaState: MangaBakaMetadataState) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        for (publisher in mangaBakaState.metadata.publishers ?: emptyList()) {
            val flagImage = when (publisher.type) {
                "Original" -> mangaBakaState.metadata.originalLanguage
                    ?.let { AppIcons.flagForLanguage(language = it, englishIsUsa = true) }

                "English" -> AppIcons.FlagUS
                else -> null
            }

            publisher.name?.let { name ->
                FilterChip(
                    selected = false,
                    onClick = {},
                    leadingIcon = {
                        flagImage?.let {
                            Image(
                                imageVector = it,
                                contentDescription = null,
                                modifier = Modifier.heightIn(max = 16.dp),
                                contentScale = ContentScale.Inside
                            )
                        }

                    },
                    label = {

                        Text(name, style = MaterialTheme.typography.labelMedium)
                    },
                    contentPadding = PaddingValues(horizontal = 5.dp)
                )

            }
        }
    }
}

@Composable
private fun AnimeInfo(mangaBakaMeta: KomfMangaBakaSeries) {
    val anime = mangaBakaMeta.anime ?: return
    anime.start?.let { start ->
        SelectionContainer {
            Column {
                Text(
                    "Anime start",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(start, style = MaterialTheme.typography.bodyMedium)
            }
        }

    }
    anime.end?.let { end ->
        SelectionContainer {
            Column {
                Text(
                    "Anime end",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(end, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun SourcesAndScores(mangaBakaMeta: KomfMangaBakaSeries) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {

        mangaBakaMeta.rating?.let { rating ->
            SourceButton(
                url = mangaBakaMeta.canonicalUrl,
                rating = rating.roundToInt(),
                image = painterResource(Res.drawable.mangabaka)
            )
        }

        val mangaUpdates = mangaBakaMeta.source.mangaUpdates
        mangaUpdates.id?.let { id ->
            SourceButton(
                url = "https://www.mangaupdates.com/series/$id",
                rating = mangaUpdates.ratingNormalized,
                image = AppIcons.MangaUpdates
            )
        }
        val aniList = mangaBakaMeta.source.anilist
        aniList.id?.let { id ->
            SourceButton(
                url = "https://anilist.co/manga/$id",
                rating = aniList.ratingNormalized,
                image = AppIcons.AniList,
            )
        }
        val myAnimeList = mangaBakaMeta.source.myAnimeList
        myAnimeList.id?.let { id ->
            SourceButton(
                url = "https://myanimelist.net/manga/$id",
                rating = myAnimeList.ratingNormalized,
                image = AppIcons.MyAnimeList
            )
        }

        val animePlanet = mangaBakaMeta.source.animePlanet
        animePlanet.id?.let { id ->
            SourceButton(
                url = "https://www.anime-planet.com/manga/$id",
                rating = animePlanet.ratingNormalized,
                image = AppIcons.AnimePlanet
            )
        }

        val kitsu = mangaBakaMeta.source.kitsu
        kitsu.id?.let { id ->
            SourceButton(
                url = "https://kitsu.app/manga/$id",
                rating = kitsu.ratingNormalized,
                image = AppIcons.Kitsu
            )
        }
        val shikimori = mangaBakaMeta.source.shikimori
        shikimori.id?.let { id ->
            SourceButton(
                url = "https://shikimori.one/mangas/$id",
                rating = shikimori.ratingNormalized,
                image = AppIcons.Shikimori,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
        }
        val animeNewsNetwork = mangaBakaMeta.source.animeNewsNetwork
        animeNewsNetwork.id?.let { id ->
            SourceButton(
                url = "https://www.animenewsnetwork.com/encyclopedia/manga.php?id=$id",
                rating = animeNewsNetwork.ratingNormalized,
                image = painterResource(Res.drawable.animeNewsNetwork)
            )
        }
    }
}


@Composable
private fun SourceButton(
    url: String,
    rating: Int?,
    image: Painter,
) {
    SourceButton(url, rating) {
        Image(painter = image, contentDescription = null, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun SourceButton(
    url: String,
    rating: Int?,
    image: ImageVector,
    colorFilter: ColorFilter? = null
) {
    SourceButton(url, rating) {
        Image(
            imageVector = image,
            contentDescription = null,
            colorFilter = colorFilter,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun SourceButton(
    url: String,
    rating: Int?,
    image: @Composable () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        state = rememberTooltipState(),
        tooltip = { PlainTooltip(maxWidth = 300.dp) { Text(url) } }
    ) {
        SuggestionChip(
            onClick = { uriHandler.openUri(url) },
            icon = { image() },
            label = {
                val rating = remember(rating) { rating?.toString() ?: "-" }
                Text(rating)
            },
            contentPadding = PaddingValues.Zero,
            border = null
        )
    }
}
