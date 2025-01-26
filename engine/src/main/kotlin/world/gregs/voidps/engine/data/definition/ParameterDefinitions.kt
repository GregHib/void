package world.gregs.voidps.engine.data.definition

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.voidps.cache.definition.Parameters
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.ParameterDefinition
import world.gregs.voidps.engine.data.yaml.decode
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

/**
 * ParameterDefinitions is responsible for decoding and managing collections of parameter definitions.
 * This class implements the `DefinitionsDecoder` interface with a specific type of `ParameterDefinition` and
 * also provides functionality from the `Parameters` interface.
 *
 * It utilizes `CategoryDefinitions` and `AmmoDefinitions` to handle relationships between parameters
 * and their respective categories or ammo groups.
 */
class ParameterDefinitions(
    private val categoryDefinitions: CategoryDefinitions,
    private val ammoDefinitions: AmmoDefinitions
) : DefinitionsDecoder<ParameterDefinition>, Parameters {

    /**
     * An overridden and late-initialized property of type Array containing elements of ParameterDefinition.
     * This property is designed to hold parameter definitions, ensuring they can be accessed once initialized.
     */
    override lateinit var definitions: Array<ParameterDefinition>
    /**
     * A Map representing unique identifiers where the key is a String,
     * and the value is an Integer. This map is initialized later and is used
     * to store mappings between string identifiers and their corresponding integer values.
     */
    override lateinit var ids: Map<String, Int>
    /**
     * A map representing parameter definitions where the key is an integer identifier
     * and the value is a string containing the associated parameter's definition or name.
     *
     * This property is expected to be initialized at runtime and provides access to
     * parameter-related metadata within the containing class.
     */
    override lateinit var parameters: Map<Int, String>
    /**
     * Logger instance used for logging within the `ParameterDefinitions` class.
     * Provides inline logging capabilities to track or debug the behavior of operations
     * related to parameter definitions.
     */
    private val logger = InlineLogger()

    /**
     * Loads parameter definitions from a YAML configuration file.
     *
     * @param yaml The YAML configuration object to use for loading. Defaults to the result of `get()`.
     * @param path The path to the parameter definitions in the YAML file. Defaults to `Settings["definitions.parameters"]`.
     * @return A `ParameterDefinitions` instance containing the loaded parameter definitions.
     */
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.parameters"]): ParameterDefinitions {
        timedLoad("parameter definition") {
            val size = decode(yaml, path) { id, key, _ ->
                ParameterDefinition(id = id, stringId = key)
            }
            parameters = definitions.associate { it.id to it.stringId }
            size
        }
        return this
    }

    /**
     * Sets the specified value in the `extras` map based on the provided `name` key.
     * The behavior of this method varies depending on the prefix or suffix of the `name` key.
     * Handles specific cases such as `equip_skill_`, `equip_level_`, `use_skill_`, `use_level_`, and more.
     *
     * @param extras A mutable map where the key-value pairs are stored. This is the target map for the operation.
     * @param name The key used to determine the specific operation to perform or the value to set in the extras map.
     * @param value The value to be added or processed in relation to the key and logic defined by the method.
     */
    @Suppress("UNCHECKED_CAST")
    override fun set(extras: MutableMap<String, Any>, name: String, value: Any) {
        when {
            name.startsWith("equip_skill_") || name.startsWith("equip_level_") -> {
                val map = extras.getOrPut("equip_req") { Object2IntOpenHashMap<Skill>(6) } as MutableMap<Skill, Int>
                if (name.startsWith("equip_skill_")) {
                    val skill = Skill.all[value as Int]
                    map[skill] = -1
                } else {
                    val skill = map.keys.firstOrNull { map[it] == -1 } ?: return logger.warn { "Missing $name $value" }
                    map[skill] = value as Int
                }
            }
            name.startsWith("use_skill_") || name.startsWith("use_level_") -> {
                val map = extras.getOrPut("skill_req") { Object2IntOpenHashMap<Skill>(6) } as MutableMap<Skill, Int>
                if (name.startsWith("use_skill_")) {
                    val skill = Skill.all[value as Int]
                    map[skill] = -1
                } else {
                    val skill = map.keys.firstOrNull { map[it] == -1 } ?: return logger.warn { "Missing $name $value" }
                    map[skill] = value as Int
                }
            }
            name.endsWith("strength") -> {
                extras[name] = (value as Int) / 10.0
            }
            name == "skillcape_skill" -> {
                extras[name] = Skill.all[value as Int]
            }
            name == "category" -> {
                val int = value as Int
                extras[name] = categoryDefinitions.get(int).stringId
            }
            name == "ammo_group" -> {
                val int = value as Int
                extras[name] = ammoDefinitions.get(int).stringId
            }
            name.startsWith("worn_option_") -> {
                val list = extras.getOrPut("worn_options") { Int2ObjectOpenHashMap<String>(4) } as MutableMap<Int, String>
                list[name.removePrefix("worn_option_").toInt() - 1] = value as String
            }
            else -> super.set(extras, name, value)
        }
    }

    /**
     * Checks if the parameter definition is empty and returns an empty instance of `ParameterDefinition`.
     *
     * This method is overridden to provide a default empty state for parameter definitions,
     * ensuring a consistent contract for handling empty or unset parameter definitions.
     *
     * @return An instance of `ParameterDefinition.EMPTY` representing an empty parameter definition.
     */
    override fun empty() = ParameterDefinition.EMPTY

}