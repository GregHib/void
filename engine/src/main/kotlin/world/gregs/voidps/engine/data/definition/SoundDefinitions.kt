package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.data.snapshot.Snapshot
import world.gregs.voidps.engine.data.config.SoundDefinition
import world.gregs.voidps.engine.timedLoad

/**
 * Officially known as Synths
 */
class SoundDefinitions : DefinitionsDecoder<SoundDefinition> {

    override lateinit var definitions: Array<SoundDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(paths: List<String>, snapshot: Snapshot? = null): SoundDefinitions {
        timedLoad("sound definition") {
            if (snapshot != null && restore(snapshot)) {
                return@timedLoad ids.size
            }
            val definitions = Array(10_000) { SoundDefinition.EMPTY }
            val ids = Object2IntOpenHashMap<String>(250, Hash.VERY_FAST_LOAD_FACTOR)
            for (path in paths) {
                Config.fileReader(path) {
                    while (nextSection()) {
                        val stringId = section()
                        require(!ids.containsKey(stringId)) { "Duplicate sound id found '$stringId' at $path." }
                        var id = 0
                        var area = false
                        while (nextPair()) {
                            when (val key = key()) {
                                "id" -> id = int()
                                "area" -> area = boolean()
                                else -> throw IllegalArgumentException("Unknown sound key: $key")
                            }
                        }
                        ids[stringId] = id
                        if (area) {
                            definitions[id] = SoundDefinition(id = id, stringId = stringId, params = mapOf(Params.AREA to true))
                        } else {
                            definitions[id] = SoundDefinition(id = id, stringId = stringId)
                        }
                    }
                }
            }
            this.definitions = definitions
            this.ids = ids
            snapshot?.write {
                ids(ids)
                // Several names can share an id, in which case the definition has the last one's
                val unique = ids.values.distinct()
                int(unique.size)
                for (id in unique) {
                    val definition = definitions[id]
                    int(id)
                    string(definition.stringId)
                    boolean(definition.params != null)
                }
            }
            ids.size
        }
        return this
    }

    private fun restore(snapshot: Snapshot): Boolean = snapshot.restore { reader ->
        val ids = reader.ids()
        val definitions = Array(10_000) { SoundDefinition.EMPTY }
        repeat(reader.int()) {
            val id = reader.int()
            val stringId = reader.string()
            definitions[id] = if (reader.boolean()) {
                SoundDefinition(id = id, stringId = stringId, params = mapOf(Params.AREA to true))
            } else {
                SoundDefinition(id = id, stringId = stringId)
            }
        }
        this.definitions = definitions
        this.ids = ids
    }

    override fun empty() = SoundDefinition.EMPTY
}
