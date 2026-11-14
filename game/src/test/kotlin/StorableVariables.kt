import world.gregs.voidps.engine.entity.character.player.Player

/**
 * The saved variables of [player] a database-backed server can't store, by name.
 *
 * `DatabaseStorage.saveVariables` takes a String, Int, Boolean, Double or Long, or a list of only Ints or only
 * Strings, and throws for anything else. Tests save to files, which take any value, so without this check a variable
 * only a database rejects (a map, say) gets through every test and fails the save on a live server.
 */
fun unstorableVariables(player: Player): Map<String, Any> = player.variables.data.filterValues { !storable(it) }

private fun storable(value: Any): Boolean = when (value) {
    is String, is Int, is Boolean, is Double, is Long -> true
    is List<*> -> value.all { it is Int } || value.all { it is String }
    else -> false
}
