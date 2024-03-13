@file:Suppress("UNCHECKED_CAST")

package world.gregs.voidps.world.interact.entity.player.combat.prayer

import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.variable.variableBitAdd
import world.gregs.voidps.engine.client.variable.variableBitRemove
import world.gregs.voidps.engine.client.variable.variableSet
import world.gregs.voidps.world.interact.entity.player.combat.prayer.PrayerConfigs.ACTIVE_CURSES
import world.gregs.voidps.world.interact.entity.player.combat.prayer.PrayerConfigs.ACTIVE_PRAYERS

variableSet("activated_*") { player ->
    player.closeInterfaces()
    val from = (from as? List<String>)?.toSet() ?: emptySet()
    val to = (to as? List<String>)?.toSet() ?: emptySet()
    for (prayer in from.subtract(to)) {
        player.emit(PrayerStop(prayer))
    }
    for (prayer in to.subtract(from)) {
        player.emit(PrayerStart(prayer))
    }
}

variableBitAdd(ACTIVE_PRAYERS, ACTIVE_CURSES) { player ->
    player.closeInterfaces()
    player.emit(PrayerStart((value as String).toSnakeCase()))
}

variableBitRemove(ACTIVE_PRAYERS, ACTIVE_CURSES) { player ->
    player.closeInterfaces()
    player.emit(PrayerStop((value as String).toSnakeCase()))
}