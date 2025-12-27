package snd.komelia.db.color

import Database
import com.juul.indexeddb.external.IDBKey
import io.github.snd_r.komelia.color.ColorLevelsPreset
import io.github.snd_r.komelia.color.repository.ColorLevelsPresetRepository
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
            objectStore(colorLevelsPresets).put(preset.toJs(), IDBKey(preset.name))
        }
    }

    override suspend fun deletePreset(preset: ColorLevelsPreset) {
        database.writeTransaction(colorLevelsPresets) {
            objectStore(colorLevelsPresets).delete(IDBKey(preset.name))
        }
    }
}