package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

object JingleLoader : Loader<JingleDefinitions> {
    val instance = JingleDefinitions()
    override val name = "jingles"
    override val files = listOf("definitions.jingles")

    override fun load(context: LoadContext): JingleDefinitions = JingleDefinitions().load(context.paths(this))

    override fun install(state: JingleDefinitions) = instance.copyFrom(state)
}
