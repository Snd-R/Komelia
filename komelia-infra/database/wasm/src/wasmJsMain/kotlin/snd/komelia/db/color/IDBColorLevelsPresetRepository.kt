package snd.komelia.db.color

import com.juul.indexeddb.Database
import snd.komelia.color.ColorLevelsPreset
import snd.komelia.color.repository.ColorLevelsPresetRepository
import snd.komelia.db.Key
import snd.komelia.db.color.jsModel.JsColorLevelsPreset
import snd.komelia.db.color.jsModel.toColorLevelsPreset
import snd.komelia.db.color.jsModel.toJs
import snd.komelia.db.colorLevelsPresets


@Suppress("UNCHECKED_CAST")
class IDBColorLevelsPresetRepository(
    private val database: Database
) : ColorLevelsPresetRepository {

    override suspend fun getPresets(): List<ColorLevelsPreset> {
        return database.transaction(colorLevelsPresets) {
            (objectStore(colorLevelsPresets).getAll() as JsArray<JsColorLevelsPreset>)
                .toList()
                .map { it.toColorLevelsPreset() }
        }
    }

    override suspend fun savePreset(preset: ColorLevelsPreset) {
        database.writeTransaction(colorLevelsPresets) {
            objectStore(colorLevelsPresets).put(preset.toJs(), Key(preset.name))
        }
    }

    override suspend fun deletePreset(preset: ColorLevelsPreset) {
        database.writeTransaction(colorLevelsPresets) {
            objectStore(colorLevelsPresets).delete(Key(preset.name))
        }
    }
}