package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.data.InterfaceDefinition

class InterfaceState(
    val definitions: Array<InterfaceDefinition>,
    val ids: Map<String, Int>,
    val componentIds: Map<String, Int>,
)
