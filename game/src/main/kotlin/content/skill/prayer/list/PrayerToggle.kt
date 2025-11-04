@file:Suppress("UNCHECKED_CAST")

package content.skill.prayer.list

import content.skill.prayer.PrayerApi
import content.skill.prayer.PrayerConfigs.ACTIVE_CURSES
import content.skill.prayer.PrayerConfigs.ACTIVE_PRAYERS
import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.entity.character.player.Player

class PrayerToggle : Script {

    init {
        variableSet("activated_*") { _, from, to ->
            closeInterfaces()
            val from = (from as? List<String>)?.toSet() ?: emptySet()
            val to = (to as? List<String>)?.toSet() ?: emptySet()
            for (prayer in from.subtract(to)) {
                PrayerApi.stop(this, prayer)
            }
            for (prayer in to.subtract(from)) {
                PrayerApi.start(this, prayer)
            }
        }

        variableBitAdded(ACTIVE_PRAYERS, ::added)
        variableBitAdded(ACTIVE_CURSES, ::added)

        variableBitRemoved(ACTIVE_PRAYERS, ::removed)
        variableBitRemoved(ACTIVE_CURSES, ::removed)
    }

    fun added(player: Player, value: Any) {
        player.closeInterfaces()
        PrayerApi.start(player, (value as String).toSnakeCase())
    }

    fun removed(player: Player, value: Any) {
        player.closeInterfaces()
        PrayerApi.stop(player, (value as String).toSnakeCase())
    }
}
