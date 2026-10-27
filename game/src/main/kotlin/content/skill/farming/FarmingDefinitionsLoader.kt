package content.skill.farming

import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

object FarmingDefinitionsLoader : Loader<FarmingDefinitions> {
    val instance = FarmingDefinitions()
    override val name = "farming"
    override val label = "farming produce definition"
    override val file = listOf("definitions.produce")

    override fun load(context: LoadContext): FarmingDefinitions = FarmingDefinitions().load(context.paths(this).single())

    override fun total(state: FarmingDefinitions) = state.diseaseChances.size

    override fun install(state: FarmingDefinitions) = instance.copyFrom(state)
}
