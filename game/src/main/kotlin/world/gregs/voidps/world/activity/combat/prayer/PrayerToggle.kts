import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.activity.combat.prayer.PrayerActivate
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.ACTIVE_CURSES
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.ACTIVE_PRAYERS
import world.gregs.voidps.world.activity.combat.prayer.PrayerDeactivate

val variables: VariableStore by inject()

on<VariableSet>({ key == ACTIVE_PRAYERS || key == ACTIVE_CURSES }) { player: Player ->
    val from = from as Int
    val to = to as Int
    val variable = variables.get(key) as BitwiseVariable<String>
    for (id in variable.values) {
        val value = variable.getValue(id) ?: continue
        if (from.has(value) && !to.has(value)) {
            player.events.emit(PrayerDeactivate(id, key == ACTIVE_CURSES))
        } else if (!from.has(value) && to.has(value)) {
            player.events.emit(PrayerActivate(id, key == ACTIVE_CURSES))
        }
    }
}

on<VariableAdded>({ key == ACTIVE_PRAYERS || key == ACTIVE_CURSES }) { player: Player ->
    player.events.emit(PrayerActivate(value as String, key == ACTIVE_CURSES))
}

on<VariableRemoved>({ key == ACTIVE_PRAYERS || key == ACTIVE_CURSES }) { player: Player ->
    player.events.emit(PrayerDeactivate(value as String, key == ACTIVE_CURSES))
}