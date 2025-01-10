package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.CategoryDefinition
import world.gregs.voidps.engine.data.yaml.decode
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

/**
 * Categories used in [ParameterDefinitions]
 */
class CategoryDefinitions : DefinitionsDecoder<CategoryDefinition> {

    override lateinit var definitions: Array<CategoryDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(yaml: Yaml = get(), path: String = Settings["definitions.categories"]): CategoryDefinitions {
        timedLoad("category definition") {
            decode(yaml, path) { id, key, _ ->
                CategoryDefinition(id = id, stringId = key)
            }
        }
        return this
    }

    override fun empty() = CategoryDefinition.EMPTY

}