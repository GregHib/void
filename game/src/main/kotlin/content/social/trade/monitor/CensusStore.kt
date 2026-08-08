package content.social.trade.monitor

import world.gregs.config.Config
import world.gregs.config.writePair
import world.gregs.config.writeSection
import world.gregs.voidps.engine.data.Settings
import java.io.File

/**
 * Persists item census player-bucket counts between server restarts.
 * Floor and exchange buckets are transient - the floor is rebuilt from
 * spawn events and exchange counts are recomputed from open offers.
 */
object CensusStore {

    fun file(): File = File(Settings["storage.players.path"]).resolve("economy/census.toml")

    fun save(counts: List<ItemCensus.Count>, file: File = file()) {
        file.parentFile.mkdirs()
        Config.fileWriter(file) {
            writeSection("players")
            for (count in counts) {
                if (count.players != 0L) {
                    writePair(count.item, count.players)
                }
            }
        }
    }

    fun load(file: File = file()): Map<String, Long> {
        if (!file.exists()) {
            return emptyMap()
        }
        val counts = mutableMapOf<String, Long>()
        Config.fileReader(file) {
            while (nextSection()) {
                when (section()) {
                    "players" -> while (nextPair()) {
                        counts[key()] = long()
                    }
                }
            }
        }
        return counts
    }
}
