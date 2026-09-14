package world.gregs.voidps.web.index

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.exposedLogger
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import world.gregs.voidps.engine.event.AuditLog
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object AuditLogIndexer {

    private const val STATE_KEY = "last_loaded_timestamp"
    private val ISO_LOCAL_FORMAT = AuditLog.ISO_LOCAL_FORMAT
    private const val BATCH_SIZE = 500

    @Volatile
    private var lastLoadedTimestamp: Long = 0L

    fun init(db: Database) {
        transaction(db) {
            SchemaUtils.create(AuditLogTable, AuditLogState)
        }
        lastLoadedTimestamp = loadLastTimestamp()
    }

    private fun loadLastTimestamp(): Long = transaction {
        AuditLogState.selectAll().where { AuditLogState.key eq STATE_KEY }
            .singleOrNull()
            ?.get(AuditLogState.value)
            ?.toLongOrNull() ?: 0L
    }

    private fun saveLastTimestamp(ts: Long) = transaction {
        AuditLogState.deleteWhere { AuditLogState.key eq STATE_KEY }
        AuditLogState.insert {
            it[key] = STATE_KEY
            it[value] = ts.toString()
        }
    }

    /**
     * Scans [directory] for AuditLog .tsv files, imports every entry newer
     * than the last recorded timestamp, and advances the bookmark.
     */
    fun index(directory: File, bots: Set<String>) {
        if (!directory.exists()) {
            return
        }
        val startHour = if (lastLoadedTimestamp == 0L) {
            println("Doing initial bulk log load. This might take a minute...")
            LocalDateTime.MIN
        } else {
            // Get start point rounded to an hour
            Instant.ofEpochMilli(lastLoadedTimestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        }.withMinute(0).withSecond(0).withNano(0)

        val relevantFiles = directory
            .listFiles { f -> f.isFile && f.name.endsWith(".tsv") }
            .mapNotNull { file ->
                val name = file.nameWithoutExtension
                val fileHour = runCatching { LocalDateTime.parse(name, ISO_LOCAL_FORMAT) }.getOrNull()
                if (fileHour != null && !fileHour.isBefore(startHour)) file to fileHour else null
            }
            .sortedBy { it.second }

        val start = System.currentTimeMillis()
        var count = 0L
        val batch = mutableListOf<ParsedEntry>()
        for ((file, _) in relevantFiles) {
            file.useLines { lines ->
                for (line in lines) {
                    if (line.isBlank()) {
                        continue
                    }
                    val entry = parseLine(line) ?: continue
                    if (entry.timestamp <= lastLoadedTimestamp || isBot(entry, bots)) {
                        continue
                    }
                    batch.add(entry)
                    if (batch.size >= BATCH_SIZE) {
                        count += flush(batch)
                        batch.clear()
                    }
                }
            }
        }
        count += flush(batch)
        batch.clear()
        println("Loaded $count logs in ${System.currentTimeMillis() - start}ms")
    }

    private fun flush(batch: List<ParsedEntry>): Int {
        if (batch.isEmpty()) {
            return 0
        }
        var maxTimestamp = lastLoadedTimestamp
        transaction {
            AuditLogTable.batchInsert(batch, shouldReturnGeneratedValues = false) { entry ->
                this[AuditLogTable.timestamp] = entry.timestamp
                this[AuditLogTable.tick] = entry.tick
                this[AuditLogTable.logSource] = entry.source
                this[AuditLogTable.action] = entry.action
                this[AuditLogTable.context] = entry.context.joinToString("\t")
            }
            maxTimestamp = batch.maxOf { it.timestamp }.coerceAtLeast(maxTimestamp)
        }
        lastLoadedTimestamp = maxTimestamp
        saveLastTimestamp(maxTimestamp)
        return batch.size
    }

    data class ParsedEntry(
        val timestamp: Long,
        val tick: Long,
        val source: String,
        val action: String,
        val context: List<String>,
    )

    private fun parseLine(line: String): ParsedEntry? {
        val parts = line.split("\t")
        if (parts.size < 4) {
            return null
        }
        val timestamp = parts[0].toLongOrNull() ?: return null
        val tick = parts[1].toLongOrNull() ?: return null
        val source = parts[2]
        val action = parts[3]
        val context = if (parts.size > 4) parts.subList(4, parts.size) else emptyList()
        return ParsedEntry(timestamp, tick, source, action, context)
    }

    private fun isBot(entry: ParsedEntry, bots: Set<String>): Boolean {
        if (entry.source.startsWith("BOT")) {
            return true
        } else if (entry.source.startsWith("PLAYER")) {
            val name = entry.source.substringAfter("PLAYER ")
            return bots.contains(name)
        }
        return false
    }
}
