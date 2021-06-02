package world.gregs.voidps.utility.func

import world.gregs.voidps.cache.definition.data.ObjectDefinition

fun ObjectDefinition.isDoor() = (name.contains("door", true) && !name.contains("trap", true)) || name.contains("gate", true)

fun ObjectDefinition.isGate() = name.contains("gate", true) && id != 10565 && id != 10566