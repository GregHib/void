package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.config.VariableDefinition

class VariablesState(
    val definitions: Map<String, VariableDefinition>,
    val varbits: Map<Int, String>,
    val varps: Map<Int, String>,
)
