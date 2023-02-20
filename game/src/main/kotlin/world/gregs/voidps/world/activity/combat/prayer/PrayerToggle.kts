package world.gregs.voidps.world.activity.combat.prayer

import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.client.variable.VariableAdded
import world.gregs.voidps.engine.client.variable.VariableRemoved
import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.ACTIVE_CURSES
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.ACTIVE_PRAYERS

on<VariableSet>({ key == ACTIVE_PRAYERS || key == ACTIVE_CURSES }) { player: Player ->
    val from = (from as List<String>).toSet()
    val to = (to as List<String>).toSet()
    for (prayer in from.subtract(to)) {
        player.events.emit(PrayerStop(prayer.toSnakeCase()))
    }
    for (prayer in to.subtract(from)) {
        player.events.emit(PrayerStart(prayer.toSnakeCase()))
    }
}

on<VariableAdded>({ key == ACTIVE_PRAYERS || key == ACTIVE_CURSES }) { player: Player ->
    player.events.emit(PrayerStart((value as String).toSnakeCase()))
}

on<VariableRemoved>({ key == ACTIVE_PRAYERS || key == ACTIVE_CURSES }) { player: Player ->
    player.events.emit(PrayerStop((value as String).toSnakeCase()))
}