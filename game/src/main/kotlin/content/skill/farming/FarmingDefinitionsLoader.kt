package content.skill.farming

import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

object FarmingDefinitionsLoader : Loader<FarmingDefinitions> {
    val instance = FarmingDefinitions()
    override val name = "farming"
    override val file = listOf("definitions.produce")

    override fun load(context: LoadContext): FarmingDefinitions = FarmingDefinitions().load(context.paths(this).single())

    override fun install(state: FarmingDefinitions) = instance.copyFrom(state)
}
