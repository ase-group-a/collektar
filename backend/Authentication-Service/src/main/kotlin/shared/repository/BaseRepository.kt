package com.collektar.shared.repository

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

abstract class BaseRepository(
    protected val database: Database
) {
    protected suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    protected suspend fun <T> dbTransaction(block: suspend () -> T): T = dbQuery {
        transaction {
            block()
        }
    }
}