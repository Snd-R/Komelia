package io.github.snd_r.komelia.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import io.github.snd_r.komelia.image.SamplerType
import io.github.snd_r.komelia.ui.reader.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.PageDisplayLayout
import io.github.snd_r.komelia.ui.reader.ReadingDirection
import io.github.snd_r.komelia.ui.series.BooksLayout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt

class AndroidSettingsRepository(
    private val dataStore: DataStore<AppSettings>
) : SettingsRepository {
    override fun getServerUrl(): Flow<String> {
        return dataStore.data.map { it.server.url }
    }

    override suspend fun putServerUrl(url: String) {
        dataStore.updateData { current ->
            current.copy {
                server = server.copy { this.url = url }
            }
        }
    }

    override fun getCardWidth(): Flow<Dp> {
        return dataStore.data
            .map {
                val width = it.appearance.cardWidth
                if (width <= 0) 180.dp
                else width.dp
            }
    }

    override suspend fun putCardWidth(cardWidth: Dp) {
        dataStore.updateData { current ->
            current.copy {
                appearance = appearance.copy { this.cardWidth = cardWidth.value.roundToInt() }
            }
        }
    }

    override fun getCurrentUser(): Flow<String> {
        return dataStore.data.map { it.user.username }
    }

    override suspend fun putCurrentUser(username: String) {
        dataStore.updateData { current ->
            current.copy {
                user = user.copy { this.username = username }
            }
        }
    }

    override fun getReaderScaleType(): Flow<LayoutScaleType> {
        return dataStore.data.map {
            when (val type = it.reader.scaleType) {
                PBLayoutScaleType.UNRECOGNIZED, null -> LayoutScaleType.SCREEN
                else -> LayoutScaleType.valueOf(type.name)
            }
        }
    }

    override suspend fun putReaderScaleType(scaleType: LayoutScaleType) {
        dataStore.updateData { current ->
            current.copy {
                reader = reader.copy { this.scaleType = PBLayoutScaleType.valueOf(scaleType.name) }
            }
        }
    }

    override fun getReaderUpsample(): Flow<Boolean> {
        return dataStore.data.map { it.reader.upsample }
    }

    override suspend fun putReaderUpsample(upsample: Boolean) {
        dataStore.updateData { current ->
            current.copy {
                reader = reader.copy { this.upsample = upsample }
            }
        }
    }

    override fun getReaderReadingDirection(): Flow<ReadingDirection> {
        return dataStore.data.map {
            when (val direction = it.reader.readingDirection) {
                PBReadingDirection.UNRECOGNIZED, null -> ReadingDirection.LEFT_TO_RIGHT
                else -> ReadingDirection.valueOf(direction.name)
            }
        }
    }

    override suspend fun putReaderReadingDirection(readingDirection: ReadingDirection) {
        dataStore.updateData { current ->
            current.copy {
                reader = reader.copy { this.readingDirection = PBReadingDirection.valueOf(readingDirection.name) }
            }
        }
    }

    override fun getReaderPageLayout(): Flow<PageDisplayLayout> {
        return dataStore.data.map {
            when (val layout = it.reader.pageLayout) {
                PBPageDisplayLayout.UNRECOGNIZED, null -> PageDisplayLayout.SINGLE_PAGE
                else -> PageDisplayLayout.valueOf(layout.name)
            }
        }
    }

    override suspend fun putReaderPageLayout(pageLayout: PageDisplayLayout) {
        dataStore.updateData { current ->
            current.copy {
                reader = reader.copy { this.pageLayout = PBPageDisplayLayout.valueOf(pageLayout.name) }
            }
        }
    }

    override fun getDecoderType(): Flow<SamplerType> {
        return flowOf(SamplerType.DEFAULT)
    }

    override suspend fun putDecoderType(type: SamplerType) {
    }

    override fun getPageLoadSize(): Flow<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun putPageLoadSize(size: Int) {
        TODO("Not yet implemented")
    }

    override fun getBookListLayout(): Flow<BooksLayout> {
        TODO("Not yet implemented")
    }

    override suspend fun putBookListLayout(layout: BooksLayout) {
        TODO("Not yet implemented")
    }
}