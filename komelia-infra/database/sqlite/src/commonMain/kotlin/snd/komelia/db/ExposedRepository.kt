package snd.komelia.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.InternalApi
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

abstract class ExposedRepository(
    private val database: Database,
) {
    @OptIn(InternalApi::class)
    protected suspend fun <T> transaction(statement: Transaction.() -> T): T {
        return withContext(Dispatchers.IO + NonCancellable) {
            suspendTransaction(db = database, statement = statement)
        }
    }
}