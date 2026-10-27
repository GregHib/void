package world.gregs.voidps.engine.data.load

import world.gregs.voidps.engine.data.snapshot.SnapshotReader
import world.gregs.voidps.engine.data.snapshot.SnapshotWriter
import java.lang.reflect.Array as JArray

/**
 * Everything a definitions singleton holds after loading: the definitions by id and their string ids.
 */
class DefinitionsState<D : Any>(
    val definitions: Array<D>,
    val ids: Map<String, Int>,
)

/**
 * Snapshots the whole [DefinitionsState], so neither the cache nor the config files need reading again.
 * @param default the return value of the restored ids for unknown names.
 */
class DefinitionsCodec<D : Any>(
    private val type: Class<D>,
    private val default: Int?,
    private val writeDefinition: SnapshotWriter.(D) -> Unit,
    private val readDefinition: SnapshotReader.() -> D,
    override val schema: Long = 1L,
) : SnapshotCodec<DefinitionsState<D>> {

    override fun write(writer: SnapshotWriter, value: DefinitionsState<D>) {
        writer.int(value.definitions.size)
        for (definition in value.definitions) {
            writer.writeDefinition(definition)
        }
        writer.ids(value.ids)
    }

    @Suppress("UNCHECKED_CAST")
    override fun read(reader: SnapshotReader): DefinitionsState<D> {
        val definitions = JArray.newInstance(type, reader.int()) as Array<D>
        for (i in definitions.indices) {
            definitions[i] = reader.readDefinition()
        }
        return DefinitionsState(definitions, reader.ids(default))
    }
}
