package world.gregs.voidps.engine.entity.item.drop

import world.gregs.voidps.engine.data.definition.ItemLoader
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

object DropTablesLoader : Loader<Map<String, DropTable>> {
    val instance = DropTables()
    override val name = "drop_tables"
    override val files = listOf("spawns.drops")
    override val dependsOn: List<Loader<*>> get() = listOf(ItemLoader)

    override fun load(context: LoadContext): Map<String, DropTable> = instance.parse(context.paths(this))

    override fun install(state: Map<String, DropTable>) {
        instance.tables = state
    }
}
