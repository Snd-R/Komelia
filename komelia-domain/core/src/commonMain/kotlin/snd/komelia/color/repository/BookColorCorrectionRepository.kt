package snd.komelia.color.repository

import kotlinx.coroutines.flow.Flow
import snd.komelia.color.BookColorLevels
import snd.komelia.color.ColorCorrectionType
import snd.komelia.color.ColorCurveBookPoints
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
