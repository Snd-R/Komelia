package snd.komelia.db.fonts

import io.github.snd_r.komelia.fonts.UserFont
import io.github.snd_r.komelia.fonts.UserFontsRepository
import kotlinx.io.files.Path
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import snd.komelia.db.ExposedRepository
import snd.komelia.db.tables.UserFontsTable

class ExposedUserFontsRepository(
    database: Database
) : ExposedRepository(database), UserFontsRepository {

    override suspend fun getAllFonts(): List<UserFont> {
        return transaction {
            UserFontsTable.selectAll().map {
                UserFont(
                    name = it[UserFontsTable.name],
                    path = Path(it[UserFontsTable.path])
                )
            }
        }
    }

    override suspend fun getFont(name: String): UserFont? {
        return transaction {
            UserFontsTable.selectAll()
                .where { UserFontsTable.name.eq(name) }
                .firstOrNull()
                ?.let {
                    UserFont(
                        name = it[UserFontsTable.name],
                        path = Path(it[UserFontsTable.path])
                    )
                }
        }
    }

    override suspend fun putFont(font: UserFont) {
        transaction {
            UserFontsTable.upsert {
                it[name] = font.name
                it[path] = font.path.toString()
            }
        }
    }

    override suspend fun deleteFont(font: UserFont) {
        transaction {
            UserFontsTable.deleteWhere { name.eq(font.name) }
        }
    }
}