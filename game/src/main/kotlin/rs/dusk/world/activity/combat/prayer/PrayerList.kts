package rs.dusk.world.activity.combat.prayer

import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.client.variable.*
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.activity.combat.prayer.PrayerConfigs.PRAYERS
import rs.dusk.world.activity.combat.prayer.PrayerConfigs.PRAYER_POINTS
import rs.dusk.world.activity.combat.prayer.PrayerConfigs.SELECTING_QUICK_PRAYERS
import rs.dusk.world.activity.combat.prayer.PrayerConfigs.USING_QUICK_PRAYERS

ListVariable(1584, Variable.Type.VARP, true, listOf(
    "normal",
    "curses"
)).register(PRAYERS)

BooleanVariable(181, Variable.Type.VARC).register(SELECTING_QUICK_PRAYERS)
BooleanVariable(182, Variable.Type.VARC).register(USING_QUICK_PRAYERS)

IntVariable(2382, Variable.Type.VARP, true, 990).register(PRAYER_POINTS)

InterfaceOpened where { name == "prayer_orb" } then {
    player.setVar(PRAYERS, "curses")
    player.sendVar(SELECTING_QUICK_PRAYERS)
    player.sendVar(USING_QUICK_PRAYERS)
}

InterfaceOpened where { name == "prayer_list" } then {
    player.sendVar(PRAYERS)
    player.sendVar(PRAYER_POINTS)
    val quickPrayers = player.getVar(SELECTING_QUICK_PRAYERS, false)
    player.interfaces.sendSetting(name, if (quickPrayers) "quick_prayers" else "regular_prayers", 0, 29, 2)
}