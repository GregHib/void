package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.snapshot.SnapshotFormat

object AreaLoader : Loader<AreasState> {
    override val name = "areas"
    override val files = listOf("map.areas")
    override val codec = SnapshotFormat.codec(AreasStateSerializer)

    override fun load(context: LoadContext): AreasState = Areas.parse(context.paths(this))

    override fun install(state: AreasState) = Areas.install(state)
}
