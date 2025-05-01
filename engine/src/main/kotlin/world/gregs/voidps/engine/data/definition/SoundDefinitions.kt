package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.engine.data.config.SoundDefinition
import world.gregs.voidps.engine.timedLoad

/**
 * Officially known as Synths
 */
class SoundDefinitions : DefinitionsDecoder<SoundDefinition> {

    override lateinit var definitions: Array<SoundDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(paths: List<String>): SoundDefinitions {
        timedLoad("sound definition") {
            val definitions = Array(10_000) { SoundDefinition.EMPTY }
            val ids = Object2IntOpenHashMap<String>(250, Hash.VERY_FAST_LOAD_FACTOR)
            for (path in paths) {
                Config.fileReader(path) {
                    while (nextSection()) {
                        val stringId = section()
                        require(!ids.containsKey(stringId)) { "Duplicate sound id found '$stringId' at $path." }
                        var id = 0
                        while (nextPair()) {
                            when (val key = key()) {
                                "id" -> id = int()
                                else -> throw IllegalArgumentException("Unknown sound key: $key")
                            }
                        }
                        ids[stringId] = id
                        definitions[id] = SoundDefinition(id = id, stringId = stringId)
                    }
                }
            }
            this.definitions = definitions
            this.ids = ids
            ids.size
        }
        return this
    }

    override fun empty() = SoundDefinition.EMPTY
}