package world.gregs.voidps.world.activity.combat.prayer

import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.entity.character.player.Player

object PrayerConfigs {
    const val PRAYERS = "prayers"

    const val SELECTING_QUICK_PRAYERS = "select_quick_prayers"
    const val USING_QUICK_PRAYERS = "using_quick_prayers"

    const val ACTIVE_PRAYERS = "activated_prayers"
    const val QUICK_PRAYERS = "quick_prayers"
    const val TEMP_QUICK_PRAYERS = "old_quick_prayers"

    const val ACTIVE_CURSES = "activated_curses"
    const val QUICK_CURSES = "quick_curses"
}

fun Player.getActivePrayerVarKey(): String = if (isCurses()) PrayerConfigs.ACTIVE_CURSES else PrayerConfigs.ACTIVE_PRAYERS

fun Player.isCurses(): Boolean = getVar(PrayerConfigs.PRAYERS, "") == "curses"