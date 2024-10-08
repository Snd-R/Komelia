package snd.komelia.db

import org.flywaydb.core.Flyway


internal actual fun flywayMigrate(filePath: String) {
    val resourcesProvider = AndroidResourcesProvider()
    Flyway(
        Flyway.configure()
            .loggers("slf4j")
            .dataSource("jdbc:sqlite:${filePath}", null, null)
            .resourceProvider(resourcesProvider)
            .javaMigrationClassProvider(resourcesProvider)
    ).migrate()
}