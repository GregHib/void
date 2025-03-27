package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.data.FontDefinition
import world.gregs.voidps.engine.timedLoad

class FontDefinitions(
    override var definitions: Array<FontDefinition>
) : DefinitionsDecoder<FontDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = FontDefinition.EMPTY

    fun load(path: String): FontDefinitions {
        timedLoad("font extra") {
            val ids = Object2IntOpenHashMap<String>(20, Hash.VERY_FAST_LOAD_FACTOR)
            Config.fileReader(path) {
                while (nextPair()) {
                    val key = key()
                    val id = int()
                    ids[key] = id
                    definitions[id].stringId = key
                }
            }
            this.ids = ids
            ids.size
        }
        return this
    }

}