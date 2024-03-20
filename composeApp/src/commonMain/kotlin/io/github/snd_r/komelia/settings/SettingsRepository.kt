package io.github.snd_r.komelia.settings

import androidx.compose.ui.unit.Dp
import io.github.snd_r.komelia.ui.reader.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.PageDisplayLayout
import io.github.snd_r.komelia.ui.reader.ReadingDirection
import kotlinx.coroutines.flow.Flow


interface SettingsRepository {

    fun getServerUrl(): Flow<String>

    suspend fun putServerUrl(url: String)

    fun getCardWidth(): Flow<Dp>

    suspend fun putCardWidth(height: Dp)

    fun getCurrentUser(): Flow<String>

    suspend fun putCurrentUser(username: String)

    fun getReaderScaleType(): Flow<LayoutScaleType>

    suspend fun putReaderScaleType(scaleType: LayoutScaleType)

    fun getReaderUpsample(): Flow<Boolean>

    suspend fun putReaderUpsample(upsample: Boolean)

    fun getReaderReadingDirection(): Flow<ReadingDirection>

    suspend fun putReaderReadingDirection(readingDirection: ReadingDirection)

    fun getReaderPageLayout(): Flow<PageDisplayLayout>

    suspend fun putReaderPageLayout(pageLayout: PageDisplayLayout)
}