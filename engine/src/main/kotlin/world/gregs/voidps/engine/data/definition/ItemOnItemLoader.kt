package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.config.ItemOnItemDefinition
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.snapshot.SnapshotFormat

object ItemOnItemLoader : Loader<Map<String, List<ItemOnItemDefinition>>> {
    val instance = ItemOnItemDefinitions()
    override val name = "item_on_item"
    override val files = listOf("definitions.itemOnItem")
    override val dependsOn: List<Loader<*>> get() = listOf(ItemLoader)
    override val codec = SnapshotFormat.codec(ItemOnItemStateSerializer)

    override fun load(context: LoadContext): Map<String, List<ItemOnItemDefinition>> = instance.parse(context.paths(this))

    override fun install(state: Map<String, List<ItemOnItemDefinition>>) = instance.install(state)
}
