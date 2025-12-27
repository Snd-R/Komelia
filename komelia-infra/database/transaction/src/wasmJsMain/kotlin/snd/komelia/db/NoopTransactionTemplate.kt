package snd.komelia.db

class NoopTransactionTemplate : TransactionTemplate {
    override suspend fun <T> execute(statement: suspend () -> T): T {
        return statement()
    }
}