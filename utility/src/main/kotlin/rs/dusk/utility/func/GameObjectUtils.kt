package rs.dusk.utility.func

import rs.dusk.cache.definition.data.ObjectDefinition

fun ObjectDefinition.isDoor() = (name.contains("door", true) && !name.contains("trap", true)) || name.contains("gate", true)