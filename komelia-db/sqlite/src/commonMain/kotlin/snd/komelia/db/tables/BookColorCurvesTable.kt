package snd.komelia.db.tables

import io.github.snd_r.komelia.color.CurvePoint
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.json.json
import snd.komelia.db.JsonDbDefault

object BookColorCurvesTable : Table("BookColorCurves") {
    val bookId = text("book_id").references(BookColorCorrectionTable.bookId)
    val colorCurvePoints = json<List<CurvePoint>>("color_curve_points", JsonDbDefault)
    val redCurvePoints = json<List<CurvePoint>>("red_curve_points", JsonDbDefault)
    val greenCurvePoints = json<List<CurvePoint>>("green_curve_points", JsonDbDefault)
    val blueCurvePoints = json<List<CurvePoint>>("blue_curve_points", JsonDbDefault)

    override val primaryKey = PrimaryKey(bookId)
}
