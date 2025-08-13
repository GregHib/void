package world.gregs.voidps.engine.data.sql

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import world.gregs.voidps.engine.data.StorageTest
import world.gregs.voidps.engine.data.DatabaseTest

class DatabaseStorageTest :
    StorageTest(),
    DatabaseTest {

    override val storage = DatabaseStorage()

    @Test
    fun `Saving variable with invalid format throws exception`() {
        assertThrows<IllegalArgumentException> {
            storage.save(listOf(save.copy(variables = mapOf("invalid_float" to 0.2f))))
        }
    }

    @Test
    fun `Load variable with invalid format throws exception`() {
        storage.save(listOf(save))
        transaction {
            val id = AccountsTable.selectAll().where { AccountsTable.name eq save.name }.first()[AccountsTable.id]
            VariablesTable.insert {
                it[playerId] = id
                it[name] = "invalid"
                it[type] = -1
            }
        }
        assertThrows<IllegalArgumentException> {
            storage.load(save.name)
        }
    }

    @Test
    fun `Load variable with missing value throws null pointer`() {
        storage.save(listOf(save))
        transaction {
            val id = AccountsTable.selectAll().where { AccountsTable.name eq save.name }.first()[AccountsTable.id]
            VariablesTable.insert {
                it[playerId] = id
                it[name] = "invalid"
                it[type] = 1
            }
        }
        assertThrows<NullPointerException> {
            storage.load(save.name)
        }
    }
}
