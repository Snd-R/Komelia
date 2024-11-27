package snd.komelia.db.fonts

import io.github.snd_r.komelia.fonts.UserFont
import io.github.snd_r.komelia.fonts.UserFontsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert
import snd.komelia.db.tables.UserFontsTable

class ExposedUserFontsRepository(
    private val database: Database
) : UserFontsRepository {

    private suspend fun <T> transactionOnDefaultDispatcher(statement: Transaction.() -> T): T {
        return withContext(Dispatchers.Default) { transaction(database, statement) }
    }

    override suspend fun getAllFonts(): List<UserFont> {
        return transactionOnDefaultDispatcher {
            UserFontsTable.selectAll().map {
                UserFont(
                    name = it[UserFontsTable.name],
                    path = Path(it[UserFontsTable.path])
                )
            }
        }
    }

    override suspend fun getFont(name: String): UserFont? {
        return transactionOnDefaultDispatcher {
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
        transactionOnDefaultDispatcher {
            UserFontsTable.upsert {
                it[name] = font.name
                it[path] = font.path.toString()
            }
        }
    }

    override suspend fun deleteFont(font: UserFont) {
        transactionOnDefaultDispatcher {
            UserFontsTable.deleteWhere { name.eq(font.name) }
        }
    }
}