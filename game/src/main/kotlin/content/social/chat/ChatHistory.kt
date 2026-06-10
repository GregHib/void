package content.social.chat

import world.gregs.voidps.engine.entity.character.player.Player
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * Tracks recent chat messages per account for use as abuse report evidence
 * Note: not thread safe; only use within game thread
 */
object ChatHistory {

    data class Entry(val time: Long, val type: String, val text: String)

    private const val MAX_ENTRIES = 20
    private val MAX_AGE = TimeUnit.MINUTES.toMillis(5)
    private val FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneOffset.UTC)
    private val history = mutableMapOf<String, ArrayDeque<Entry>>()

    fun add(player: Player, type: String, text: String) {
        val entries = history.getOrPut(player.accountName) { ArrayDeque() }
        entries.addLast(Entry(System.currentTimeMillis(), type, text))
        trim(entries)
    }

    /**
     * Snapshot of the recent messages sent by [account], formatted for storage
     */
    fun recent(account: String): List<String> {
        val entries = history[account] ?: return emptyList()
        trim(entries)
        return entries.map { "[${FORMAT.format(Instant.ofEpochMilli(it.time))}] ${it.type}: ${it.text}" }
    }

    fun clear(account: String) {
        history.remove(account)
    }

    private fun trim(entries: ArrayDeque<Entry>) {
        val expired = System.currentTimeMillis() - MAX_AGE
        while (entries.isNotEmpty() && (entries.size > MAX_ENTRIES || entries.first().time < expired)) {
            entries.removeFirst()
        }
    }
}
