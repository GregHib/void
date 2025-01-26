package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.CategoryDefinition
import world.gregs.voidps.engine.data.yaml.decode
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

/**
 * CategoryDefinitions is responsible for handling the decoding and management
 * of category definition data. It extends DefinitionsDecoder with a specific
 * type of CategoryDefinition.
 */
class CategoryDefinitions : DefinitionsDecoder<CategoryDefinition> {

    /**
     * An overridden and late-initialized property that holds an array of `CategoryDefinition` objects.
     * This property is intended to store definitions related to categories.
     */
    override lateinit var definitions: Array<CategoryDefinition>
    /**
     * A map representing a collection of string keys associated with integer values.
     * This property is meant to be overridden and initialized later.
     * Each key-value pair in the map signifies a unique identifier (key)
     * and its corresponding numeric representation (value).
     */
    override lateinit var ids: Map<String, Int>

    /**
     * Loads category definitions from the provided YAML configuration and path.
     *
     * @param yaml The YAML configuration source to use. Defaults to the result of `get()`.
     * @param path The file path or key in the configuration specifying the category definitions location. Defaults to `Settings["definitions.categories"]`.
     * @return The loaded category definitions.
     */
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.categories"]): CategoryDefinitions {
        timedLoad("category definition") {
            decode(yaml, path) { id, key, _ ->
                CategoryDefinition(id = id, stringId = key)
            }
        }
        return this
    }

    /**
     * Returns an empty instance of `CategoryDefinition`.
     *
     * This method provides a default or placeholder value when no specific category definition is present.
     * The returned instance is predefined as `CategoryDefinition.EMPTY`.
     *
     * @return An empty `CategoryDefinition` instance.
     */
    override fun empty() = CategoryDefinition.EMPTY

}