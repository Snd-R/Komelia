package snd.komelia.db

import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.sqlite.SQLiteDataSource
import javax.sql.DataSource

class Database(private val filePath: String) {
    private val datasource: DataSource
    val dsl: DSLContext


    init {
        System.getProperties().setProperty("org.jooq.no-logo", "true")
        System.getProperties().setProperty("org.jooq.no-tips", "true")
        flywayMigrate(filePath)

        datasource = SQLiteDataSource().apply {
            url = "jdbc:sqlite:${filePath}"
            databaseName = "komelia"
        }
        dsl = DSL.using(datasource, SQLDialect.SQLITE)
    }
}

internal expect fun flywayMigrate(filePath: String)