package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

object DiangoCodeLoader : Loader<DiangoCodeDefinitions> {
    val instance = DiangoCodeDefinitions()
    override val name = "diango_codes"
    override val file = listOf("definitions.diangoCodes")
    override val dependsOn: List<Loader<*>> get() = listOf(ItemLoader)

    override fun load(context: LoadContext): DiangoCodeDefinitions = DiangoCodeDefinitions().load(context.paths(this).single())

    override fun install(state: DiangoCodeDefinitions) = instance.copyFrom(state)
}
