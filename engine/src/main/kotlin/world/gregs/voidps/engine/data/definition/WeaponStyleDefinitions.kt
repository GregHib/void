package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.WeaponStyleDefinition
import world.gregs.voidps.engine.data.yaml.decode
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

/**
 * WeaponStyleDefinitions is a decoder class responsible for managing weapon style definitions.
 * It extends DefinitionsDecoder to provide functionalities for loading, decoding, and storing weapon style data.
 */
class WeaponStyleDefinitions : DefinitionsDecoder<WeaponStyleDefinition> {

    /**
     * An array of WeaponStyleDefinition objects.
     * This variable is overridden and initialized at a later stage.
     * It is used to store definitions of various weapon styles.
     */
    override lateinit var definitions: Array<WeaponStyleDefinition>
    /**
     * A map representing unique identifiers.
     *
     * The key is a string representing the identifier name,
     * and the value is an integer associated with that identifier.
     *
     * This property is meant to be overridden and initialized
     * with the desired mapping in subclasses or implementations.
     */
    override lateinit var ids: Map<String, Int>

    /**
     * Loads weapon style definitions from the specified YAML file and path.
     *
     * @param yaml the YAML object to use for loading, defaults to the result of `get()`
     * @param path the path to the definitions within the YAML file, defaults to `Settings["definitions.weapons.styles"]`
     * @return the loaded WeaponStyleDefinitions object
     */
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.weapons.styles"]): WeaponStyleDefinitions {
        timedLoad("weapon style definition") {
            decode(yaml, path) { id, key, extras ->
                WeaponStyleDefinition.fromMap(id, key, extras!!)
            }
        }
        return this
    }

    /**
     * This method overrides the `empty` function and provides an implementation
     * that returns the default empty value for `WeaponStyleDefinition`.
     *
     * It is designed to provide a pre-defined empty or null-like instance
     * of `WeaponStyleDefinition` that can be used as a placeholder or default value
     * to signify the lack of a specific weapon style definition.
     *
     * @return The default EMPTY instance of `WeaponStyleDefinition`.
     */
    override fun empty() = WeaponStyleDefinition.EMPTY
}