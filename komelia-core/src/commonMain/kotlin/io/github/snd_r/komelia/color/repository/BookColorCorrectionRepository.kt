package io.github.snd_r.komelia.color.repository

import io.github.snd_r.komelia.color.ColorCurveBookPoints
import io.github.snd_r.komelia.color.BookColorLevels
import io.github.snd_r.komelia.ui.color.ColorCorrectionType
import kotlinx.coroutines.flow.Flow
import snd.komga.client.book.KomgaBookId

interface BookColorCorrectionRepository {
    fun getCurrentType(bookId: KomgaBookId): Flow<ColorCorrectionType?>
    suspend fun setCurrentType(bookId: KomgaBookId, type: ColorCorrectionType)
    suspend fun deleteSettings(bookId: KomgaBookId)

    fun getCurve(bookId: KomgaBookId): Flow<ColorCurveBookPoints?>
    suspend fun saveCurve(points: ColorCurveBookPoints)
    suspend fun deleteCurve(bookId: KomgaBookId)

    fun getLevels(bookId: KomgaBookId): Flow<BookColorLevels?>
    suspend fun saveLevels(levels: BookColorLevels)
    suspend fun deleteLevels(bookId: KomgaBookId)
}
