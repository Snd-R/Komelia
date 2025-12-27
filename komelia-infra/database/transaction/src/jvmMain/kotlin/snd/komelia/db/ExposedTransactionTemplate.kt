package snd.komelia.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class ExposedTransactionTemplate(private val database: Database) : TransactionTemplate {
    override suspend fun <T> execute(statement: suspend () -> T): T {
        return withContext(Dispatchers.IO + NonCancellable) {
            suspendTransaction(db = database) { statement() }
        }
    }
}