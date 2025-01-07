package snd.komelia.db.color

import io.github.snd_r.komelia.color.BookColorLevels
import io.github.snd_r.komelia.color.ColorCurveBookPoints
import io.github.snd_r.komelia.color.ColorCurvePoints
import io.github.snd_r.komelia.color.ColorLevelChannels
import io.github.snd_r.komelia.color.ColorLevelsConfig
import io.github.snd_r.komelia.color.repository.BookColorCorrectionRepository
import io.github.snd_r.komelia.ui.color.ColorCorrectionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import snd.komelia.db.ExposedRepository
import snd.komelia.db.tables.BookColorCorrectionTable
import snd.komelia.db.tables.BookColorCurvesTable
import snd.komelia.db.tables.BookColorLevelsTable
import snd.komga.client.book.KomgaBookId

class ExposedBookColorCorrectionRepository(
    database: Database
) : ExposedRepository(database), BookColorCorrectionRepository {
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
        return transaction {
            BookColorCorrectionTable.selectAll()
                .where { BookColorCorrectionTable.bookId.eq(bookId.value) }
                .firstOrNull()
                ?.let { ColorCorrectionType.valueOf(it[BookColorCorrectionTable.type]) }
        }
    }

    override suspend fun setCurrentType(bookId: KomgaBookId, type: ColorCorrectionType) {
        transaction {
            BookColorCorrectionTable.upsert {
                it[this.bookId] = bookId.value
                it[this.type] = type.name
            }
        }
        typeChangeFlow.emit(bookId to type)
    }

    override suspend fun deleteSettings(bookId: KomgaBookId) {
        transaction {
            BookColorCurvesTable.deleteWhere { BookColorCurvesTable.bookId.eq(bookId.value) }
            BookColorLevelsTable.deleteWhere { BookColorLevelsTable.bookId.eq(bookId.value) }
            BookColorCorrectionTable.deleteWhere { BookColorCorrectionTable.bookId.eq(bookId.value) }
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
        return transaction {
            BookColorCurvesTable.selectAll()
                .where { BookColorCurvesTable.bookId.eq(bookId.value) }
                .firstOrNull()
                ?.let {
                    val points = ColorCurvePoints(
                        colorCurvePoints = it[BookColorCurvesTable.colorCurvePoints],
                        redCurvePoints = it[BookColorCurvesTable.redCurvePoints],
                        greenCurvePoints = it[BookColorCurvesTable.greenCurvePoints],
                        blueCurvePoints = it[BookColorCurvesTable.blueCurvePoints],
                    )
                    ColorCurveBookPoints(
                        bookId = KomgaBookId(it[BookColorCurvesTable.bookId]),
                        channels = points
                    )
                }
        }
    }

    override suspend fun saveCurve(points: ColorCurveBookPoints) {
        transaction {
            BookColorCurvesTable.upsert {
                it[this.bookId] = points.bookId.value
                it[colorCurvePoints] = points.channels.colorCurvePoints
                it[redCurvePoints] = points.channels.redCurvePoints
                it[greenCurvePoints] = points.channels.greenCurvePoints
                it[blueCurvePoints] = points.channels.blueCurvePoints
            }
        }
        curveChangeFlow.emit(points.bookId to points)
    }

    override suspend fun deleteCurve(bookId: KomgaBookId) {
        transaction {
            BookColorCurvesTable.deleteWhere { BookColorCurvesTable.bookId.eq(bookId.value) }
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
        return transaction {
            BookColorLevelsTable.selectAll()
                .where { BookColorLevelsTable.bookId.eq(bookId.value) }
                .firstOrNull()
                ?.let {
                    val channels = ColorLevelChannels(
                        color = ColorLevelsConfig(
                            lowInput = it[BookColorLevelsTable.colorLowInput],
                            highInput = it[BookColorLevelsTable.colorHighInput],
                            lowOutput = it[BookColorLevelsTable.colorLowOutput],
                            highOutput = it[BookColorLevelsTable.colorHighOutput],
                            gamma = it[BookColorLevelsTable.colorGamma],
                        ),
                        red = ColorLevelsConfig(
                            lowInput = it[BookColorLevelsTable.redLowInput],
                            highInput = it[BookColorLevelsTable.redHighInput],
                            lowOutput = it[BookColorLevelsTable.redLowOutput],
                            highOutput = it[BookColorLevelsTable.redHighOutput],
                            gamma = it[BookColorLevelsTable.redGamma],
                        ),
                        green = ColorLevelsConfig(
                            lowInput = it[BookColorLevelsTable.greenLowInput],
                            highInput = it[BookColorLevelsTable.greenHighInput],
                            lowOutput = it[BookColorLevelsTable.greenLowOutput],
                            highOutput = it[BookColorLevelsTable.greenHighOutput],
                            gamma = it[BookColorLevelsTable.greenGamma],
                        ),
                        blue = ColorLevelsConfig(
                            lowInput = it[BookColorLevelsTable.blueLowInput],
                            highInput = it[BookColorLevelsTable.blueHighInput],
                            lowOutput = it[BookColorLevelsTable.blueLowOutput],
                            highOutput = it[BookColorLevelsTable.blueHighOutput],
                            gamma = it[BookColorLevelsTable.blueGamma],
                        ),
                    )
                    BookColorLevels(
                        bookId = KomgaBookId(it[BookColorLevelsTable.bookId]),
                        channels = channels
                    )
                }
        }
    }

    override suspend fun saveLevels(levels: BookColorLevels) {
        transaction {
            BookColorLevelsTable.upsert {
                it[this.bookId] = levels.bookId.value
                it[colorLowInput] = levels.channels.color.lowInput
                it[colorHighInput] = levels.channels.color.highInput
                it[colorLowOutput] = levels.channels.color.lowOutput
                it[colorHighOutput] = levels.channels.color.highOutput
                it[colorGamma] = levels.channels.color.gamma

                it[redLowInput] = levels.channels.red.lowInput
                it[redHighInput] = levels.channels.red.highInput
                it[redLowOutput] = levels.channels.red.lowOutput
                it[redHighOutput] = levels.channels.red.highOutput
                it[redGamma] = levels.channels.red.gamma

                it[greenLowInput] = levels.channels.green.lowInput
                it[greenHighInput] = levels.channels.green.highInput
                it[greenLowOutput] = levels.channels.green.lowOutput
                it[greenHighOutput] = levels.channels.green.highOutput
                it[greenGamma] = levels.channels.green.gamma

                it[blueLowInput] = levels.channels.blue.lowInput
                it[blueHighInput] = levels.channels.blue.highInput
                it[blueLowOutput] = levels.channels.blue.lowOutput
                it[blueHighOutput] = levels.channels.blue.highOutput
                it[blueGamma] = levels.channels.blue.gamma
            }
        }
        levelsChangeFlow.emit(levels.bookId to levels)
    }

    override suspend fun deleteLevels(bookId: KomgaBookId) {
        transaction {
            BookColorLevelsTable.deleteWhere { BookColorLevelsTable.bookId.eq(bookId.value) }
        }
        levelsChangeFlow.emit(bookId to null)
    }
}