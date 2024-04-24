package io.github.snd_r.komelia.settings

import androidx.compose.ui.unit.Dp
import io.github.snd_r.komelia.platform.SamplerType
import io.github.snd_r.komelia.ui.series.BooksLayout
import kotlinx.coroutines.flow.Flow


interface SettingsRepository {

    fun getServerUrl(): Flow<String>
    suspend fun putServerUrl(url: String)

    fun getCardWidth(): Flow<Dp>
    suspend fun putCardWidth(cardWidth: Dp)

    fun getCurrentUser(): Flow<String>
    suspend fun putCurrentUser(username: String)

    fun getSeriesPageLoadSize(): Flow<Int>
    suspend fun putSeriesPageLoadSize(size: Int)

    fun getBookPageLoadSize(): Flow<Int>
    suspend fun putBookPageLoadSize(size: Int)

    fun getBookListLayout(): Flow<BooksLayout>
    suspend fun putBookListLayout(layout: BooksLayout)

    fun getDecoderType(): Flow<SamplerType>
    suspend fun putDecoderType(type: SamplerType)
}