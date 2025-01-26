package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.config.data.QuestDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.yaml.decode
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

/**
 * Represents a decoder for quest definitions. This class is responsible for loading, storing,
 * and processing definitions related to quests in the application.
 *
 * The `QuestDefinitions` class decodes YAML-based quest definition files and provides access
 * to the parsed definitions as well as their corresponding IDs.
 */
class QuestDefinitions : DefinitionsDecoder<QuestDefinition> {

    /**
     * This property holds an array of `QuestDefinition` objects.
     * It is overridden and marked as `lateinit`, which means it will be initialized at a later point
     * before it is accessed.
     */
    override lateinit var definitions: Array<QuestDefinition>
    /**
     * A map containing string keys and integer values, used to store and retrieve
     * identification values associated with specific keys.
     *
     * This property is defined as a lateinit variable and must be initialized
     * before it is accessed. The override modifier indicates that this variable
     * overrides a similar property in a superclass or interface.
     */
    override lateinit var ids: Map<String, Int>

    /**
     * Loads quest definitions from the specified YAML configuration file and path.
     *
     * @param yaml The YAML parser to use for decoding the quest definitions. Defaults to the result of `get()`.
     * @param path The path to the YAML file containing the quest definitions. Defaults to the value of `Settings["definitions.quests"]`.
     * @return A `QuestDefinitions` object populated with the decoded quest definitions.
     */
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.quests"]): QuestDefinitions {
        timedLoad("quest definition") {
            decode(yaml, path) { id, key, extras ->
                QuestDefinition(id = id, stringId = key, extras = extras)
            }
        }
        return this
    }

    /**
     * Provides an implementation of the `empty` method, returning an empty or default instance of `QuestDefinition`.
     *
     * This method is designed to supply a standardized empty state for `QuestDefinition` objects.
     * It is particularly useful in situations where an empty or placeholder quest definition is required,
     * ensuring consistency and preventing null references.
     *
     * @return The predefined empty instance of `QuestDefinition`, represented by `QuestDefinition.EMPTY`.
     */
    override fun empty() = QuestDefinition.EMPTY

}