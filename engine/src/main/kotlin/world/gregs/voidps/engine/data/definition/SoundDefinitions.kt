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

    fun load(path: String): SoundDefinitions {
        timedLoad("sound definition") {
            val definitions = Array(10_000) { SoundDefinition.EMPTY }
            val ids = Object2IntOpenHashMap<String>(250, Hash.VERY_FAST_LOAD_FACTOR)
            Config.fileReader(path) {
                while (nextPair()) {
                    val stringId = key()
                    val id = int()
                    require(!ids.containsKey(stringId)) { "Duplicate sound id found '$stringId' at $path." }
                    ids[stringId] = id
                    definitions[id] = SoundDefinition(id = id, stringId = stringId)
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