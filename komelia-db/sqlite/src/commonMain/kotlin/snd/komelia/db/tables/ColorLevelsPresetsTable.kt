package snd.komelia.db.tables

import org.jetbrains.exposed.sql.Table

object ColorLevelsPresetsTable : Table("ColorLevelsPresets") {
    val name = text("name")

    val colorLowInput = float("color_low_in")
    val colorHighInput = float("color_high_in")
    val colorLowOutput = float("color_low_out")
    val colorHighOutput = float("color_high_out")
    val colorGamma = float("color_gamma")

    val redLowInput = float("red_low_in")
    val redHighInput = float("red_high_in")
    val redLowOutput = float("red_low_out")
    val redHighOutput = float("red_high_out")
    val redGamma = float("red_gamma")

    val greenLowInput = float("green_low_in")
    val greenHighInput = float("green_high_in")
    val greenLowOutput = float("green_low_out")
    val greenHighOutput = float("green_high_out")
    val greenGamma = float("green_gamma")

    val blueLowInput = float("blue_low_in")
    val blueHighInput = float("blue_high_in")
    val blueLowOutput = float("blue_low_out")
    val blueHighOutput = float("blue_high_out")
    val blueGamma = float("blue_gamma")

    override val primaryKey = PrimaryKey(name)
}
