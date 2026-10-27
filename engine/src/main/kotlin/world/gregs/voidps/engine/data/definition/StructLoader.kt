package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.config.data.StructDefinition
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.engine.data.load.DefinitionsState
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.snapshot.SnapshotFormat
import world.gregs.voidps.engine.data.snapshot.serializer.DefinitionsStateSerializer

object StructLoader : Loader<DefinitionsState<StructDefinition>> {
    override val name = "structs"
    override val file = listOf("definitions.structs")
    override val usesCache = true
    override val codec = SnapshotFormat.codec(DefinitionsStateSerializer(StructDefinition::class, StructDefinition.serializer(), default = null))

    override fun load(context: LoadContext): DefinitionsState<StructDefinition> {
        val definitions = StructDecoder().load(context.requireCache())
        return DefinitionsState(definitions, parse(definitions, context.paths(this).single()))
    }

    override fun install(state: DefinitionsState<StructDefinition>) = StructDefinitions.install(state)

    fun parse(definitions: Array<StructDefinition>, path: String): Map<String, Int> {
        val ids = Object2IntOpenHashMap<String>(512, Hash.VERY_FAST_LOAD_FACTOR)
        Config.fileReader(path) {
            while (nextPair()) {
                val stringId = key()
                val id = int()
                require(!ids.containsKey(stringId)) { "Duplicate struct id found '$stringId' at $path." }
                ids[stringId] = id
                definitions[id].stringId = stringId
            }
        }
        return ids
    }
}
