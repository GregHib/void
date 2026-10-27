package content.entity.obj

import it.unimi.dsi.fastutil.Hash
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.snapshot.SnapshotFormat
import world.gregs.voidps.engine.data.snapshot.serializer.IntKeyedMapSerializer
import world.gregs.voidps.engine.data.snapshot.serializer.StringKeyedMapSerializer

object ObjectTeleportLoader : Loader<Map<String, Map<Int, ObjectTeleports.TeleportDefinition>>> {
    val instance = ObjectTeleports()
    override val name = "object_teleports"
    override val files = listOf("map.teleports")
    override val codec = SnapshotFormat.codec(StringKeyedMapSerializer(IntKeyedMapSerializer(ObjectTeleports.TeleportDefinition.serializer()), Hash.VERY_FAST_LOAD_FACTOR))

    override fun load(context: LoadContext): Map<String, Map<Int, ObjectTeleports.TeleportDefinition>> = instance.parse(context.paths(this))

    override fun install(state: Map<String, Map<Int, ObjectTeleports.TeleportDefinition>>) = instance.install(state)
}
