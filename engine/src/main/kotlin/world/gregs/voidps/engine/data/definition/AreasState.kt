package world.gregs.voidps.engine.data.definition

class AreasState(
    val named: Map<String, AreaDefinition>,
    val tagged: Map<String, Set<AreaDefinition>>,
    val areas: Map<Int, Set<AreaDefinition>>,
)
