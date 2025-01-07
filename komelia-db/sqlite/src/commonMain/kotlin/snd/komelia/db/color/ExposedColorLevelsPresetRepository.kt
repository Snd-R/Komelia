package snd.komelia.db.color

import io.github.snd_r.komelia.color.ColorLevelChannels
import io.github.snd_r.komelia.color.ColorLevelsConfig
import io.github.snd_r.komelia.color.ColorLevelsPreset
import io.github.snd_r.komelia.color.repository.ColorLevelsPresetRepository
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import snd.komelia.db.ExposedRepository
import snd.komelia.db.tables.ColorLevelsPresetsTable

class ExposedColorLevelsPresetRepository(
    database: Database
) : ExposedRepository(database), ColorLevelsPresetRepository {

    override suspend fun getPresets(): List<ColorLevelsPreset> {
        return transaction {
            ColorLevelsPresetsTable.selectAll()
                .map {
                    val channels = ColorLevelChannels(
                        color = ColorLevelsConfig(
                            lowInput = it[ColorLevelsPresetsTable.colorLowInput],
                            highInput = it[ColorLevelsPresetsTable.colorHighInput],
                            lowOutput = it[ColorLevelsPresetsTable.colorLowOutput],
                            highOutput = it[ColorLevelsPresetsTable.colorHighOutput],
                            gamma = it[ColorLevelsPresetsTable.colorGamma],
                        ),
                        red = ColorLevelsConfig(
                            lowInput = it[ColorLevelsPresetsTable.redLowInput],
                            highInput = it[ColorLevelsPresetsTable.redHighInput],
                            lowOutput = it[ColorLevelsPresetsTable.redLowOutput],
                            highOutput = it[ColorLevelsPresetsTable.redHighOutput],
                            gamma = it[ColorLevelsPresetsTable.redGamma],
                        ),
                        green = ColorLevelsConfig(
                            lowInput = it[ColorLevelsPresetsTable.greenLowInput],
                            highInput = it[ColorLevelsPresetsTable.greenHighInput],
                            lowOutput = it[ColorLevelsPresetsTable.greenLowOutput],
                            highOutput = it[ColorLevelsPresetsTable.greenHighOutput],
                            gamma = it[ColorLevelsPresetsTable.greenGamma],
                        ),
                        blue = ColorLevelsConfig(
                            lowInput = it[ColorLevelsPresetsTable.blueLowInput],
                            highInput = it[ColorLevelsPresetsTable.blueHighInput],
                            lowOutput = it[ColorLevelsPresetsTable.blueLowOutput],
                            highOutput = it[ColorLevelsPresetsTable.blueHighOutput],
                            gamma = it[ColorLevelsPresetsTable.blueGamma],
                        ),
                    )
                    ColorLevelsPreset(
                        name = it[ColorLevelsPresetsTable.name],
                        channels = channels
                    )
                }
        }
    }

    override suspend fun savePreset(preset: ColorLevelsPreset) {
        transaction {
            ColorLevelsPresetsTable.upsert {
                it[this.name] = preset.name
                it[colorLowInput] = preset.channels.color.lowInput
                it[colorHighInput] = preset.channels.color.highInput
                it[colorLowOutput] = preset.channels.color.lowOutput
                it[colorHighOutput] = preset.channels.color.highOutput
                it[colorGamma] = preset.channels.color.gamma

                it[redLowInput] = preset.channels.red.lowInput
                it[redHighInput] = preset.channels.red.highInput
                it[redLowOutput] = preset.channels.red.lowOutput
                it[redHighOutput] = preset.channels.red.highOutput
                it[redGamma] = preset.channels.red.gamma

                it[greenLowInput] = preset.channels.green.lowInput
                it[greenHighInput] = preset.channels.green.highInput
                it[greenLowOutput] = preset.channels.green.lowOutput
                it[greenHighOutput] = preset.channels.green.highOutput
                it[greenGamma] = preset.channels.green.gamma

                it[blueLowInput] = preset.channels.blue.lowInput
                it[blueHighInput] = preset.channels.blue.highInput
                it[blueLowOutput] = preset.channels.blue.lowOutput
                it[blueHighOutput] = preset.channels.blue.highOutput
                it[blueGamma] = preset.channels.blue.gamma
            }
        }
    }

    override suspend fun deletePreset(preset: ColorLevelsPreset) {
        transaction { ColorLevelsPresetsTable.deleteWhere { name.eq(preset.name) } }
    }
}