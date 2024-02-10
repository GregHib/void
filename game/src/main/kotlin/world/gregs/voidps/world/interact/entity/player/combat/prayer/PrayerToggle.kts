@file:Suppress("UNCHECKED_CAST")

package world.gregs.voidps.world.interact.entity.player.combat.prayer

import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.variable.variableAdded
import world.gregs.voidps.engine.client.variable.variableRemoved
import world.gregs.voidps.engine.client.variable.variableSet
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.world.interact.entity.player.combat.prayer.PrayerConfigs.ACTIVE_CURSES
import world.gregs.voidps.world.interact.entity.player.combat.prayer.PrayerConfigs.ACTIVE_PRAYERS

variableSet("activated_*") { player: Player ->
    player.closeInterfaces()
    val from = (from as? List<String>)?.toSet() ?: emptySet()
    val to = (to as? List<String>)?.toSet() ?: emptySet()
    for (prayer in from.subtract(to)) {
        player.events.emit(PrayerStop(prayer))
    }
    for (prayer in to.subtract(from)) {
        player.events.emit(PrayerStart(prayer))
    }
}

variableAdded(ACTIVE_PRAYERS, ACTIVE_CURSES) { player: Player ->
    player.closeInterfaces()
    player.events.emit(PrayerStart((value as String).toSnakeCase()))
}

variableRemoved(ACTIVE_PRAYERS, ACTIVE_CURSES) { player: Player ->
    player.closeInterfaces()
    player.events.emit(PrayerStop((value as String).toSnakeCase()))
}