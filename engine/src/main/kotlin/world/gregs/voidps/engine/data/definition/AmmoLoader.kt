package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

object AmmoLoader : Loader<AmmoDefinitions> {
    val instance = AmmoDefinitions()
    override val name = "ammo"
    override val file = listOf("definitions.ammoGroups")

    override fun load(context: LoadContext): AmmoDefinitions = AmmoDefinitions().load(context.paths(this).single())

    override fun install(state: AmmoDefinitions) = instance.copyFrom(state)
}
