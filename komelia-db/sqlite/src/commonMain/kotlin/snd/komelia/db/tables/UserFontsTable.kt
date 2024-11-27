package snd.komelia.db.tables

import org.jetbrains.exposed.sql.Table

object UserFontsTable : Table("UserFonts") {
    val name = text("name")
    val path = text("path")

    override val primaryKey = PrimaryKey(name)
}