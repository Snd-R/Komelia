package snd.komelia.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

abstract class ExposedRepository(
    private val database: Database
) {
    protected suspend fun <T> transactionOnDefaultDispatcher(statement: Transaction.() -> T): T {
        return withContext(Dispatchers.Default) { transaction(database, statement) }
    }
}