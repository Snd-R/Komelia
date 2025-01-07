package snd.komelia.db.color

import Database
import com.juul.indexeddb.external.IDBKey
import io.github.snd_r.komelia.color.ColorCurvePreset
import io.github.snd_r.komelia.color.repository.ColorCurvePresetRepository
import snd.komelia.db.color.jsModel.JsColorCurvePreset
import snd.komelia.db.color.jsModel.toColorCurvePreset
import snd.komelia.db.color.jsModel.toJs
import snd.komelia.db.colorCurvePresets


@Suppress("UNCHECKED_CAST")
class IDBColorCurvesPresetRepository(private val database: Database) : ColorCurvePresetRepository {

    override suspend fun getPresets(): List<ColorCurvePreset> {
        return database.transaction(colorCurvePresets) {
            (objectStore(colorCurvePresets).getAll() as JsArray<JsColorCurvePreset>)
                .toList()
                .map { it.toColorCurvePreset() }
        }
    }

    override suspend fun savePreset(preset: ColorCurvePreset) {
        database.writeTransaction(colorCurvePresets) {
            objectStore(colorCurvePresets).put(preset.toJs(), IDBKey(preset.name))
        }
    }

    override suspend fun deletePreset(preset: ColorCurvePreset) {
        database.writeTransaction(colorCurvePresets) {
            objectStore(colorCurvePresets).delete(IDBKey(preset.name))
        }
    }
}