package world.gregs.voidps.storage

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import world.gregs.voidps.engine.data.AbuseReport
import world.gregs.voidps.engine.data.CensusSnapshot
import world.gregs.voidps.engine.data.EconomyFlag
import java.util.concurrent.TimeUnit
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
    fun `Store economy flags`() {
        val flag = EconomyFlag(
            type = "trade_value",
            timestamp = 1234567890123,
            tick = 4242,
            source = "durial321",
            target = "cow1337killa",
            item = null,
            value = 25_000_000,
            details = "requester=25000000 acceptor=0",
        )

        storage.saveEconomyFlags(listOf(flag, flag.copy(type = "price_anomaly", source = null, target = null, item = "party_hat", details = null)))

        transaction {
            val rows = EconomyFlagsTable.selectAll().orderBy(EconomyFlagsTable.id).toList()
            assertEquals(2, rows.size)
            assertEquals(flag.type, rows[0][EconomyFlagsTable.type])
            assertEquals(flag.timestamp, rows[0][EconomyFlagsTable.timestamp])
            assertEquals(flag.tick, rows[0][EconomyFlagsTable.tick])
            assertEquals(flag.source, rows[0][EconomyFlagsTable.sourceName])
            assertEquals(flag.target, rows[0][EconomyFlagsTable.targetName])
            assertEquals(null, rows[0][EconomyFlagsTable.item])
            assertEquals(flag.value, rows[0][EconomyFlagsTable.value])
            assertEquals(flag.details, rows[0][EconomyFlagsTable.details])
            assertEquals("party_hat", rows[1][EconomyFlagsTable.item])
            assertEquals(null, rows[1][EconomyFlagsTable.sourceName])
        }
    }

    @Test
    fun `Store census snapshots`() {
        val snapshot = CensusSnapshot(
            timestamp = 1234567890123,
            item = "coins",
            players = 2_147_483_648,
            floor = 1000,
            exchange = 500_000,
        )

        storage.saveCensusSnapshots(listOf(snapshot))

        transaction {
            val row = EconomyCensusTable.selectAll().single()
            assertEquals(snapshot.timestamp, row[EconomyCensusTable.timestamp])
            assertEquals(snapshot.item, row[EconomyCensusTable.item])
            assertEquals(snapshot.players, row[EconomyCensusTable.players])
            assertEquals(snapshot.floor, row[EconomyCensusTable.floor])
            assertEquals(snapshot.exchange, row[EconomyCensusTable.exchange])
        }
    }

    @Test
    fun `Prune old economy rows`() {
        val now = 1234567890123
        val old = now - TimeUnit.DAYS.toMillis(91)
        storage.saveEconomyFlags(
            listOf(
                EconomyFlag(type = "trade_value", timestamp = now, tick = 1),
                EconomyFlag(type = "trade_value", timestamp = old, tick = 0),
            ),
        )
        storage.saveCensusSnapshots(
            listOf(
                CensusSnapshot(timestamp = now, item = "coins", players = 1, floor = 0, exchange = 0),
                CensusSnapshot(timestamp = old, item = "coins", players = 2, floor = 0, exchange = 0),
            ),
        )

        storage.pruneEconomy(now - TimeUnit.DAYS.toMillis(90))

        transaction {
            assertEquals(now, EconomyFlagsTable.selectAll().single()[EconomyFlagsTable.timestamp])
            assertEquals(now, EconomyCensusTable.selectAll().single()[EconomyCensusTable.timestamp])
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
