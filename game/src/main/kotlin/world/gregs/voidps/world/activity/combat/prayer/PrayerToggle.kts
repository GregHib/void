import world.gregs.voidps.engine.client.variable.VariableAdded
import world.gregs.voidps.engine.client.variable.VariableRemoved
import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.client.variable.has
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.entity.stop
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.utility.func.toUnderscoreCase
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.ACTIVE_CURSES
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.ACTIVE_PRAYERS

val variables: VariableDefinitions by inject()

on<VariableSet>({ key == ACTIVE_PRAYERS || key == ACTIVE_CURSES }) { player: Player ->
    val from = from as Int
    val to = to as Int
    val variable = variables.get(key) ?: return@on
    println(variable.values)
    with(player.variables) {
        val values = variable.values as List<Any>
        for (id in values) {
            val value = variable.getValue(id) ?: continue
            if (from.has(value) && !to.has(value)) {
                player.stop("prayer_${(id as String).toUnderscoreCase()}")
            } else if (!from.has(value) && to.has(value)) {
                player.start("prayer_${(id as String).toUnderscoreCase()}")
            }
        }
    }
}

on<VariableAdded>({ key == ACTIVE_PRAYERS || key == ACTIVE_CURSES }) { player: Player ->
    player.start("prayer_${(value as String).toUnderscoreCase()}")
}

on<VariableRemoved>({ key == ACTIVE_PRAYERS || key == ACTIVE_CURSES }) { player: Player ->
    player.stop("prayer_${(value as String).toUnderscoreCase()}")
}