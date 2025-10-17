package snd.komelia.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

abstract class ExposedRepository(
    private val database: Database
) {
    protected suspend fun <T> transaction(
        statement: Transaction.() -> T
    ): T {
        return withContext(Dispatchers.Default) {
            transaction(db = database, statement = statement)
        }
    }
}