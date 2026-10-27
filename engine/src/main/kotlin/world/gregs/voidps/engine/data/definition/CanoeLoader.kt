package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

object CanoeLoader : Loader<CanoeDefinitions> {
    val instance = CanoeDefinitions()
    override val name = "canoes"
    override val file = listOf("map.canoes")

    override fun load(context: LoadContext): CanoeDefinitions = CanoeDefinitions().load(context.paths(this).single())

    override fun install(state: CanoeDefinitions) = instance.copyFrom(state)
}
