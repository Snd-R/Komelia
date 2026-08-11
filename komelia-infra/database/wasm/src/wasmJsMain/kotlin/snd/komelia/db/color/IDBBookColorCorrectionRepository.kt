package snd.komelia.db.color

import com.juul.indexeddb.Database
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import snd.komelia.color.BookColorLevels
import snd.komelia.color.ColorCorrectionType
import snd.komelia.color.ColorCurveBookPoints
import snd.komelia.color.repository.BookColorCorrectionRepository
import snd.komelia.db.Key
import snd.komelia.db.color.jsModel.JsBookColorCorrection
import snd.komelia.db.color.jsModel.JsBookColorLevels
import snd.komelia.db.color.jsModel.JsColorCurveBookPoints
import snd.komelia.db.color.jsModel.jsBookColorCorrection
import snd.komelia.db.color.jsModel.toBookColorLevels
import snd.komelia.db.color.jsModel.toColorCurveBookPoints
import snd.komelia.db.color.jsModel.toJs
import snd.komelia.db.colorCorrectionStore
import snd.komelia.db.colorCurvesStore
import snd.komelia.db.colorLevelsStore
import snd.komga.client.book.KomgaBookId


@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
class IDBBookColorCorrectionRepository(
    private val database: Database
) : BookColorCorrectionRepository {
    private val typeChangeFlow = MutableSharedFlow<Pair<KomgaBookId, ColorCorrectionType?>>()
    private val curveChangeFlow = MutableSharedFlow<Pair<KomgaBookId, ColorCurveBookPoints?>>()
    private val levelsChangeFlow = MutableSharedFlow<Pair<KomgaBookId, BookColorLevels?>>()

    override fun getCurrentType(bookId: KomgaBookId): Flow<ColorCorrectionType?> {
        return flow {
            emit(fetchType(bookId))
            typeChangeFlow.collect { (id, type) -> if (id == bookId) emit(type) }
        }.distinctUntilChanged()
    }

    private suspend fun fetchType(bookId: KomgaBookId): ColorCorrectionType? {
        return database.transaction(colorCorrectionStore) {
            (objectStore(colorCorrectionStore).get(Key(bookId.value)) as? JsBookColorCorrection)?.let {
                ColorCorrectionType.valueOf(it.type)
            }
        }
    }

    override suspend fun setCurrentType(bookId: KomgaBookId, type: ColorCorrectionType) {
        database.writeTransaction(colorCorrectionStore) {
            val store = objectStore(colorCorrectionStore)
            store.put(jsBookColorCorrection(bookId, type), Key(bookId.value))
        }
        typeChangeFlow.emit(bookId to type)
    }

    override suspend fun deleteSettings(bookId: KomgaBookId) {
        database.writeTransaction(
            colorCorrectionStore,
            colorCurvesStore,
            colorLevelsStore
        ) {
            val correctionStore = objectStore(colorCorrectionStore)
            val curvesStore = objectStore(colorCurvesStore)
            val levelsStore = objectStore(colorLevelsStore)

            curvesStore.delete(Key(bookId.value))
            levelsStore.delete(Key(bookId.value))
            correctionStore.delete(Key(bookId.value))
        }
        curveChangeFlow.emit(bookId to null)
        levelsChangeFlow.emit(bookId to null)
        typeChangeFlow.emit(bookId to null)
    }

    override fun getCurve(bookId: KomgaBookId): Flow<ColorCurveBookPoints?> {
        return flow {
            emit(fetchCurve(bookId))
            curveChangeFlow.collect { (id, value) -> if (bookId == id) emit(value) }
        }.distinctUntilChanged()
    }

    private suspend fun fetchCurve(bookId: KomgaBookId): ColorCurveBookPoints? {
        return database.transaction(colorCurvesStore) {
            (objectStore(colorCurvesStore).get(Key(bookId.value)) as? JsColorCurveBookPoints)
                ?.toColorCurveBookPoints()
        }
    }

    override suspend fun saveCurve(points: ColorCurveBookPoints) {
        database.writeTransaction(colorCurvesStore) {
            objectStore(colorCurvesStore).put(points.toJs(), Key(points.bookId.value))
        }
        curveChangeFlow.emit(points.bookId to points)
    }

    override suspend fun deleteCurve(bookId: KomgaBookId) {
        database.writeTransaction(colorCurvesStore) {
            objectStore(colorCurvesStore).delete(Key(bookId.value))
        }
        curveChangeFlow.emit(bookId to null)
    }

    override fun getLevels(bookId: KomgaBookId): Flow<BookColorLevels?> {
        return flow {
            emit(fetchLevels(bookId))
            levelsChangeFlow.collect { (id, value) -> if (bookId == id) emit(value) }
        }
    }

    private suspend fun fetchLevels(bookId: KomgaBookId): BookColorLevels? {
        return database.transaction(colorLevelsStore) {
            (objectStore(colorLevelsStore).get(Key(bookId.value)) as? JsBookColorLevels)
                ?.toBookColorLevels()
        }
    }

    override suspend fun saveLevels(levels: BookColorLevels) {
        database.writeTransaction(colorLevelsStore) {
            objectStore(colorLevelsStore).put(levels.toJs(), Key(levels.bookId.value))
        }
        levelsChangeFlow.emit(levels.bookId to levels)
    }

    override suspend fun deleteLevels(bookId: KomgaBookId) {
        database.writeTransaction(colorLevelsStore) {
            objectStore(colorLevelsStore).delete(Key(bookId.value))
        }
        levelsChangeFlow.emit(bookId to null)
    }
}