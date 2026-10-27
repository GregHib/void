package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

object PatrolLoader : Loader<PatrolDefinitions> {
    val instance = PatrolDefinitions()
    override val name = "patrols"
    override val label = "patrol definition"
    override val files = listOf("definitions.patrols")

    override fun load(context: LoadContext): PatrolDefinitions = PatrolDefinitions().load(context.paths(this))

    override fun total(state: PatrolDefinitions) = state.definitions.size

    override fun install(state: PatrolDefinitions) = instance.copyFrom(state)
}
