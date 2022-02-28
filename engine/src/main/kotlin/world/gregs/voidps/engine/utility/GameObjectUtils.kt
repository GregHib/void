package world.gregs.voidps.engine.utility

import world.gregs.voidps.cache.definition.data.ObjectDefinition

fun ObjectDefinition.isDoor() = (name.contains("door", true) && !name.contains("trap", true)) || name.contains("gate", true)

fun ObjectDefinition.isGate() = name.contains("gate", true) && id != 10565 && id != 10566 && id != 28690 && id != 28691