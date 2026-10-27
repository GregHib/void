package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

object AmmoLoader : Loader<AmmoDefinitions> {
    val instance = AmmoDefinitions()
    override val name = "ammo"
    override val label = "ammo definition"
    override val file = listOf("definitions.ammoGroups")

    override fun load(context: LoadContext): AmmoDefinitions = AmmoDefinitions().load(context.paths(this).single())

    override fun total(state: AmmoDefinitions) = state.ids.size

    override fun install(state: AmmoDefinitions) = instance.copyFrom(state)
}
