package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.MidiDefinition
import world.gregs.voidps.engine.data.yaml.decode
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

/**
 * Represents a specific implementation of `DefinitionsDecoder` for MIDI definitions.
 * This class handles the loading, decoding, and management of `MidiDefinition` objects,
 * including their mappings to both integer and string-based identifiers.
 */
class MidiDefinitions : DefinitionsDecoder<MidiDefinition> {

    /**
     * An overridden and late-initialized array of `MidiDefinition` objects.
     * This variable is intended to store MIDI configuration or definitions
     * and must be initialized before use.
     */
    override lateinit var definitions: Array<MidiDefinition>
    /**
     * A mapping of string identifiers to their respective integer IDs for MIDI definitions.
     *
     * This map associates a unique string identifier with its corresponding numeric ID,
     * which is used to reference specific MIDI definitions within the application.
     *
     * Key: The string identifier for a MIDI definition.
     * Value: The numeric ID associated with the string identifier.
     */
    override lateinit var ids: Map<String, Int>

    /**
     * Loads MIDI definitions from the specified YAML configuration file.
     *
     * @param yaml An instance of the `Yaml` object for parsing the configuration file. Defaults to the result of `get()`.
     * @param path The file path to the YAML configuration containing MIDI definitions. Defaults to the value of `Settings["definitions.midis"]`.
     * @return The loaded `MidiDefinitions` object.
     */
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.midis"]): MidiDefinitions {
        timedLoad("midi definition") {
            decode(yaml, path) { id, key, _ ->
                MidiDefinition(id = id, stringId = key)
            }
        }
        return this
    }

    /**
     * Returns an empty instance of `MidiDefinition`.
     *
     * This method is used to provide a default or placeholder `MidiDefinition` when no specific definition is available.
     *
     * @return The empty instance of `MidiDefinition`, defined as `MidiDefinition.EMPTY`.
     */
    override fun empty() = MidiDefinition.EMPTY

}