package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.AmmoDefinition
import world.gregs.voidps.engine.data.yaml.decode
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

/**
 * The `AmmoDefinitions` class is responsible for decoding and managing a collection of ammo definitions.
 * It inherits from `DefinitionsDecoder` with a specific type of `AmmoDefinition`.
 */
class AmmoDefinitions : DefinitionsDecoder<AmmoDefinition> {

    /**
     * An overridden, late-initialized array holding instances of `AmmoDefinition`.
     * This variable is expected to be initialized before its usage.
     * Typically used to store and manage multiple ammunition definitions.
     */
    override lateinit var definitions: Array<AmmoDefinition>
    /**
     * A map of IDs where the key represents a string identifier and the value represents an integer ID.
     * This property is overridden and expected to be initialized before accessing it.
     * It can be used to store and retrieve mappings between string names and their corresponding integer IDs.
     */
    override lateinit var ids: Map<String, Int>

    /**
     * Loads the ammo definitions from the specified YAML file and path.
     *
     * This method decodes the YAML content at the given path to create and populate
     * instances of `AmmoDefinition`. It provides a timed loading process for ammo definitions.
     *
     * @param yaml An instance of the `Yaml` class used to parse the YAML content. By default,
     *             it uses the instance returned from the `get()` method.
     * @param path A string representing the path to the YAML content where ammo definitions are stored.
     *             By default, it is fetched from the `Settings` using the key `definitions.ammoGroups`.
     * @return Returns the current object as an instance of `AmmoDefinitions` after successfully loading
     *         and decoding the YAML content.
     */
    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.ammoGroups"]): AmmoDefinitions {
        timedLoad("ammo definition") {
            decode(yaml, path) { id, key, extras ->
                val items = extras?.get("items") as? List<String>
                AmmoDefinition(
                    id = id,
                    items = if (items != null) ObjectOpenHashSet(items) else emptySet(),
                    stringId = key
                )
            }
        }
        return this
    }

    /**
     * Overrides the `empty` method to provide an implementation
     * that returns an empty instance of `AmmoDefinition`.
     *
     * @return The `AmmoDefinition.EMPTY` instance representing an empty state.
     */
    override fun empty() = AmmoDefinition.EMPTY

}