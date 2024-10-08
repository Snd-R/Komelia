package snd.komelia.db

import org.flywaydb.core.Flyway

internal actual fun flywayMigrate(filePath: String) {
    Flyway(
        Flyway.configure()
            .loggers("slf4j")
            .dataSource("jdbc:sqlite:${filePath}", null, null)
            .failOnMissingLocations(true)
            .locations("classpath:composeResources/io.github.snd_r.sqlite.generated.resources/files")
    ).migrate()
}