package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.SoundDefinition
import world.gregs.voidps.engine.data.yaml.decode
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

/**
 * Handles the decoding and management of sound definitions. It provides functionality to load,
 * process, and manage sound definition objects, represented by the `SoundDefinition` class.
 */
class SoundDefinitions : DefinitionsDecoder<SoundDefinition> {

    /**
     * This property holds an array of `SoundDefinition` objects. It is defined as `lateinit`, meaning
     * it should be initialized before accessing. The property is declared as `override`, suggesting
     * that it is inherited and implements or overrides a member from a superclass or interface.
     */
    override lateinit var definitions: Array<SoundDefinition>
    /**
     * A map associating string identifiers to their corresponding integer IDs for sound definitions.
     *
     * This variable is intended to provide quick access to sound definition IDs
     * through their unique string identifiers.
     */
    override lateinit var ids: Map<String, Int>

    /**
     * Loads sound definitions from the provided YAML configuration.
     *
     * @param yaml The YAML configuration object to decode. Defaults to the result of `get()`.
     * @param path The path within the settings to retrieve sound definitions. Defaults to `Settings["definitions.sounds"]`.
     * @return SoundDefinitions instance populated with the loaded sound definitions.
     */
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.sounds"]): SoundDefinitions {
        timedLoad("sound definition") {
            decode(yaml, path) { id, key, _ ->
                SoundDefinition(id = id, stringId = key)
            }
        }
        return this
    }

    /**
     * Provides an empty or default instance of [SoundDefinition].
     *
     * This method returns a predefined `EMPTY` instance of [SoundDefinition], which serves as a placeholder
     * or default value when no specific sound definition is available.
     *
     * @return The `EMPTY` instance of [SoundDefinition].
     */
    override fun empty() = SoundDefinition.EMPTY
}