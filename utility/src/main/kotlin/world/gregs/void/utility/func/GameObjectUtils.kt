package world.gregs.void.utility.func

import world.gregs.void.cache.definition.data.ObjectDefinition

fun ObjectDefinition.isDoor() = (name.contains("door", true) && !name.contains("trap", true)) || name.contains("gate", true)