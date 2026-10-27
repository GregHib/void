package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

object HuntModeLoader : Loader<HuntModeDefinitions> {
    val instance = HuntModeDefinitions()
    override val name = "hunt_modes"
    override val file = listOf("definitions.huntModes")
    override val dependsOn: List<Loader<*>> get() = listOf(NPCLoader, ObjectLoader, ItemLoader)

    override fun load(context: LoadContext): HuntModeDefinitions = HuntModeDefinitions().load(context.paths(this).single())

    override fun install(state: HuntModeDefinitions) = instance.copyFrom(state)
}
