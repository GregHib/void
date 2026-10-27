package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.snapshot.SnapshotFormat

object VariableLoader : Loader<VariablesState> {
    override val name = "variables"
    override val label = "variable definition"
    override val files = listOf(
        "definitions.variables.players",
        "definitions.variables.bits",
        "definitions.variables.clients",
        "definitions.variables.strings",
        "definitions.variables.customs",
    )
    override val codec = SnapshotFormat.codec(VariablesStateSerializer)

    override fun load(context: LoadContext): VariablesState = VariableDefinitions.parse(
        context.list("definitions.variables.players"),
        context.list("definitions.variables.bits"),
        context.list("definitions.variables.clients"),
        context.list("definitions.variables.strings"),
        context.list("definitions.variables.customs"),
    )

    override fun total(state: VariablesState) = state.definitions.size

    override fun install(state: VariablesState) = VariableDefinitions.install(state)
}
