package snd.komelia.db

import Database
import VersionChangeTransaction
import openDatabase

internal const val colorCorrectionStore = "bookColorCorrection"
internal const val colorCurvesStore = "bookColorCurves"
internal const val colorLevelsStore = "bookColorLevels"

internal const val colorCurvePresets = "colorCurvePresets"
internal const val colorLevelsPresets = "colorLevelsPresets"

private const val currentVersion = 1

suspend fun getIndexedDb() = openDatabase("Komelia", currentVersion) { database, oldVersion, newVersion ->
    if (oldVersion < 1) {
        version1Migrate(database)
    }
}


private fun VersionChangeTransaction.version1Migrate(database: Database) {
    database.createObjectStore(colorCorrectionStore)
    database.createObjectStore(colorCurvesStore)
    database.createObjectStore(colorLevelsStore)
    database.createObjectStore(colorCurvePresets)
    database.createObjectStore(colorLevelsPresets)
}