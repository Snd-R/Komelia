package snd.komelia.db

import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import java.sql.Connection

class KomeliaDatabase(filePath: String) {
    val database: Database

    init {
        flywayMigrate(filePath)

        database = Database.connect("jdbc:sqlite:${filePath}")
        TransactionManager.defaultDatabase = database
        // no concurrent writes in sqlite
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    }
}

private fun flywayMigrate(filePath: String) {
    val resourcesProvider = MigrationResourcesProvider()
    Flyway(
        Flyway.configure()
            .loggers("slf4j")
            .dataSource("jdbc:sqlite:${filePath}", null, null)
            .resourceProvider(resourcesProvider)
            .javaMigrationClassProvider(resourcesProvider)
    ).migrate()
}