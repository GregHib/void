import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.entity.stop
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.utility.func.toUnderscoreCase
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.ACTIVE_CURSES
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.ACTIVE_PRAYERS

val variables: VariableStore by inject()

on<VariableSet>({ key == ACTIVE_PRAYERS || key == ACTIVE_CURSES }) { player: Player ->
    val from = from as Int
    val to = to as Int
    val variable = variables.get(key) as BitwiseVariable<String>
    for (id in variable.values) {
        val value = variable.getValue(id) ?: continue
        if (from.has(value) && !to.has(value)) {
            player.stop("prayer_${id.toUnderscoreCase()}")
        } else if (!from.has(value) && to.has(value)) {
            player.start("prayer_${id.toUnderscoreCase()}")
        }
    }
}

on<VariableAdded>({ key == ACTIVE_PRAYERS || key == ACTIVE_CURSES }) { player: Player ->
    player.start("prayer_${(value as String).toUnderscoreCase()}")
}

on<VariableRemoved>({ key == ACTIVE_PRAYERS || key == ACTIVE_CURSES }) { player: Player ->
    player.stop("prayer_${(value as String).toUnderscoreCase()}")
}