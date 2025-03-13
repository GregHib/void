package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.CategoryDefinition
import world.gregs.voidps.engine.timedLoad

/**
 * Categories used in [ParameterDefinitions]
 */
class CategoryDefinitions : DefinitionsDecoder<CategoryDefinition> {

    override lateinit var definitions: Array<CategoryDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(path: String = Settings["definitions.categories"]): CategoryDefinitions {
        timedLoad("category definition") {
            val ids = Object2IntOpenHashMap<String>(38, Hash.VERY_FAST_LOAD_FACTOR)
            val definitions = Array(38) { CategoryDefinition.EMPTY }
            val reader = object : ConfigReader(50) {
                override fun set(section: String, key: String, value: Any) {
                    if (section == "categories") {
                        val id = (value as Long).toInt()
                        ids[key] = id
                        definitions[id] = CategoryDefinition(id, key)
                    }
                }
            }
            Config.decodeFromFile(path, reader)
            this.definitions = definitions
            this.ids = ids
            ids.size
        }
        return this
    }

    override fun empty() = CategoryDefinition.EMPTY

}