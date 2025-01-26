package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.VariableDefinition
import world.gregs.voidps.engine.data.yaml.DefinitionIdsConfig
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml
import java.io.File
import kotlin.collections.set

/**
 * A class responsible for managing and storing variable definitions, as well as providing access to those definitions.
 * This includes mappings for variable definitions, varbit IDs, and varp IDs.
 */
class VariableDefinitions {

    /**
     * A map containing variable definitions where the key is a string representing
     * the variable's name and the value is an instance of `VariableDefinition`.
     * This map initializes as empty by default.
     */
    private var definitions: Map<String, VariableDefinition> = emptyMap()
    /**
     * A map that associates unique Varbit IDs with their corresponding names.
     *
     * These Varbit IDs are often used to reference specific configurable variables
     * within the system, defined by their unique integer keys and their associated string identifiers.
     * The map is initialized as empty by default.
     */
    private var varbitIds: Map<Int, String> = emptyMap()
    /**
     * A map representing variable parameter (varp) identifiers and their associated names.
     *
     * The map's keys are the identifiers (integers), and the associated values are their string representations.
     * This serves as a lookup table for varp definitions within the context of variable definitions.
     */
    private var varpIds: Map<Int, String> = emptyMap()

    /**
     * Retrieves a value from the `definitions` map corresponding to the specified key.
     *
     * @param key The key used to lookup the value in the `definitions` map.
     * @return The value associated with the provided key, or null if the key is not present.
     */
    fun get(key: String) = definitions[key]

    /**
     * Retrieves a varbit value by its unique identifier.
     *
     * @param id The unique identifier of the varbit to retrieve.
     * @return The varbit value associated with the given identifier.
     */
    fun getVarbit(id: Int) = varbitIds[id]

    /**
     * Retrieves the variable parameter value associated with the specified ID.
     *
     * @param id The identifier of the variable parameter to retrieve.
     * @return The value of the variable parameter corresponding to the given ID.
     */
    fun getVarp(id: Int) = varpIds[id]

    /**
     * Loads variable definitions from YAML files located at the specified path.
     *
     * @param yaml the `Yaml` instance used for loading the definitions, defaults to a singleton instance.
     * @param path the directory path where the YAML files are located, defaults to the "definitions.path" setting.
     * @return the instance of `VariableDefinitions` that contains the loaded definitions.
     */
    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.path"]): VariableDefinitions {
        timedLoad("variable definition") {
            val definitions = Object2ObjectOpenHashMap<String, VariableDefinition>()
            val files = File(path).listFiles()?.filter { it.name.startsWith("variables-") } ?: emptyList()
            val varbitIds = Int2ObjectOpenHashMap<String>()
            val varpIds = Int2ObjectOpenHashMap<String>()
            for (file in files) {
                val type = file.nameWithoutExtension.removePrefix("variables-")
                val factory: (Map<String, Any>) -> VariableDefinition = when (type) {
                    "player" -> {
                        { VariableDefinition.VarpDefinition(it) }
                    }
                    "player-bit" -> {
                        { VariableDefinition.VarbitDefinition(it) }
                    }
                    "client" -> {
                        { VariableDefinition.VarcDefinition(it) }
                    }
                    "client-string" -> {
                        { VariableDefinition.VarcStrDefinition(it) }
                    }
                    else -> {
                        { VariableDefinition.CustomVariableDefinition(it) }
                    }
                }
                val config = object : DefinitionIdsConfig() {
                    /**
                     * Overrides the `set` method to handle specific logic for updating a map with a key-value pair.
                     *
                     * @param map The mutable map in which the key-value pair will be updated or stored.
                     * @param key The key with which the specified value will be associated.
                     * @param value The value to be associated with the specified key in the map.
                     * @param indent The indentation level determining the behavior of this method.
                     * @param parentMap Optional parameter representing the parent map if applicable.
                     */
                    override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                        if (indent == 0) {
                            val definition = factory.invoke(if (value is Int) {
                                mapOf("id" to value)
                            } else {
                                value as Map<String, Any>
                            })
                            definitions[key] = definition
                            if (type == "player") {
                                varpIds[definition.id] = key
                            } else if (type == "player-bit") {
                                varbitIds[definition.id] = key
                            }
                        } else {
                            super.set(map, key, value, indent, parentMap)
                        }
                    }
                }
                yaml.load<Any>(file.path, config)
            }
            this.varbitIds = varbitIds
            this.varpIds = varpIds
            this.definitions = definitions
            this.definitions.size
        }
        return this
    }
}