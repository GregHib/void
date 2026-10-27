package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

object RenderEmoteLoader : Loader<RenderEmoteDefinitions> {
    val instance = RenderEmoteDefinitions()
    override val name = "render_emotes"
    override val file = listOf("definitions.renderEmotes")

    override fun load(context: LoadContext): RenderEmoteDefinitions = RenderEmoteDefinitions().load(context.paths(this).single())

    override fun install(state: RenderEmoteDefinitions) = instance.copyFrom(state)
}
