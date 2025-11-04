package content.skill.prayer

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.entity.character.player.Player

interface PrayerApi {

    fun prayerStart(prayer: String = "*", block: Player.(prayer: String) -> Unit) {
        prayerStarted.getOrPut(prayer) { mutableListOf() }.add(block)
    }

    fun prayerStop(prayer: String = "*", block: Player.(prayer: String) -> Unit) {
        prayerStopped.getOrPut(prayer) { mutableListOf() }.add(block)
    }

    companion object : AutoCloseable {
        private val prayerStarted = Object2ObjectOpenHashMap<String, MutableList<Player.(String) -> Unit>>(2)
        private val prayerStopped = Object2ObjectOpenHashMap<String, MutableList<Player.(String) -> Unit>>(2)

        fun start(player: Player, prayer: String) {
            for (block in prayerStarted[prayer] ?: emptyList()) {
                block(player, prayer)
            }
            for (block in prayerStarted["*"] ?: return) {
                block(player, prayer)
            }
        }

        fun stop(player: Player, prayer: String) {
            for (block in prayerStopped[prayer] ?: emptyList()) {
                block(player, prayer)
            }
            for (block in prayerStopped["*"] ?: return) {
                block(player, prayer)
            }
        }

        override fun close() {
            prayerStarted.clear()
            prayerStopped.clear()
        }
    }
}
