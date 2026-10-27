package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.snapshot.SnapshotFormat
import kotlin.collections.set

object TableLoader : Loader<TablesState> {
    override val name = "tables"
    override val files = listOf("definitions.tables")
    override val dependsOn: List<Loader<*>> get() = listOf(ItemLoader, ObjectLoader, NPCLoader, VariableLoader, AnimationLoader, GraphicLoader)
    override val codec = SnapshotFormat.codec(TablesStateSerializer)

    override fun load(context: LoadContext): TablesState = Tables.parse(context.paths(this))

    override fun install(state: TablesState) = Tables.install(state)
}
