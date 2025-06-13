package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.engine.data.config.CategoryDefinition
import world.gregs.voidps.engine.timedLoad

/**
 * Official Categories used in [ParameterDefinitions]
 */
class CategoryDefinitions : DefinitionsDecoder<CategoryDefinition> {

    override lateinit var definitions: Array<CategoryDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(path: String): CategoryDefinitions {
        timedLoad("category definition") {
            val ids = Object2IntOpenHashMap<String>(38, Hash.VERY_FAST_LOAD_FACTOR)
            val definitions = Array(38) { CategoryDefinition.EMPTY }
            Config.fileReader(path, 50) {
                while (nextPair()) {
                    val key = key()
                    val id = int()
                    ids[key] = id
                    definitions[id] = CategoryDefinition(id, key)
                }
            }
            this.definitions = definitions
            this.ids = ids
            ids.size
        }
        return this
    }

    override fun empty() = CategoryDefinition.EMPTY
}
