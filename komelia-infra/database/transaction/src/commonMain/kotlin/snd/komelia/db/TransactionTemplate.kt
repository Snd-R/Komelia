package snd.komelia.db

interface TransactionTemplate {
    suspend fun <T> execute(statement: suspend () -> T): T
}