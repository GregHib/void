package content.skill.prayer

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player

fun Player.prayersBlocked(): Boolean = Areas.get(tile.zone).any { "no_prayer" in it.tags && tile in it.area }

class PrayerRestrictions : Script {
    init {
        entered("*") { area ->
            if ("no_prayer" in area.tags) {
                clear(PrayerConfigs.ACTIVE_PRAYERS)
                clear(PrayerConfigs.ACTIVE_CURSES)
                set(PrayerConfigs.USING_QUICK_PRAYERS, false)
            }
        }
    }
}
