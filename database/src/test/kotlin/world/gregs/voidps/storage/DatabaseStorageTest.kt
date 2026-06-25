package world.gregs.voidps.storage

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import world.gregs.voidps.engine.data.AbuseReport
import kotlin.test.assertEquals

class DatabaseStorageTest : StorageTest(), DatabaseTest {

    override val storage = DatabaseStorage()

    @Test
    fun `Store an abuse report`() {
        val report = AbuseReport(
            reporter = "mod_steve",
            reported = "Durial321",
            rule = 6,
            ruleName = "Macroing",
            mute = true,
            suggestion = "extra info",
            time = 1234567890,
            evidence = listOf("[00:00:01] public: free armour trimming", "[00:00:02] public: selling gf"),
        )

        storage.saveReport(report)

        transaction {
            val row = ReportsTable.selectAll().single()
            assertEquals(report.reporter, row[ReportsTable.reporter])
            assertEquals(report.reported, row[ReportsTable.reported])
            assertEquals(report.rule, row[ReportsTable.rule])
            assertEquals(report.ruleName, row[ReportsTable.ruleName])
            assertEquals(report.mute, row[ReportsTable.mute])
            assertEquals(report.suggestion, row[ReportsTable.suggestion])
            assertEquals(report.time, row[ReportsTable.time])
            assertEquals(report.evidence, row[ReportsTable.evidence])
        }
    }

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
