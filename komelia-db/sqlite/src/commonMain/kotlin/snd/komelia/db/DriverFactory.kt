package snd.komelia.db

import app.cash.sqldelight.db.SqlDriver

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): Database {
    val dbDriver = driverFactory.createDriver()
    val database = Database(dbDriver)
    return database
}
