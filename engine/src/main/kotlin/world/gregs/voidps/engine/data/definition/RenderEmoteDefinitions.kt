package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.RenderEmoteDefinition
import world.gregs.voidps.engine.data.yaml.decode
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

/**
 * Handles the decoding and management of `RenderEmoteDefinition` instances.
 * Inherits from the `DefinitionsDecoder` interface and provides specific implementations
 * for loading, managing, and retrieving render emote definitions.
 */
class RenderEmoteDefinitions : DefinitionsDecoder<RenderEmoteDefinition> {

    /**
     * An overridden, late-initialized property that holds an array of `RenderEmoteDefinition` objects.
     * These definitions are likely used to configure or control the rendering of emotes within the application.
     */
    override lateinit var definitions: Array<RenderEmoteDefinition>
    /**
     * A map where the keys are string identifiers and the values are integer IDs for render emote definitions.
     *
     * This map serves as a lookup table to associate string identifiers with their corresponding
     * integer IDs. It is typically populated during the decoding of render emote definitions and
     * used for quick access or reference by the string-based keys.
     */
    override lateinit var ids: Map<String, Int>

    /**
     * Loads render emote definitions from the specified YAML source and path, and initializes
     * them using the provided decoding logic.
     *
     * @param yaml The YAML configuration object to load the definitions from. Defaults to the result of `get()`.
     * @param path The path within the settings to locate the definitions. Defaults to the value of `Settings["definitions.renderEmotes"]`.
     * @return A [RenderEmoteDefinitions] object containing the loaded render emote definitions.
     */
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.renderEmotes"]): RenderEmoteDefinitions {
        timedLoad("render emote definition") {
            decode<RenderEmoteDefinition>(yaml, path) { id, key, _ ->
                RenderEmoteDefinition(id = id, stringId = key)
            }
        }
        return this
    }

    /**
     * Provides an empty or default instance of `RenderEmoteDefinition`.
     *
     * This method is used to return a placeholder or default value when no specific
     * `RenderEmoteDefinition` instance is available. Typically used in scenarios where
     * an absence of data needs to be representable with a neutral or predefined placeholder.
     *
     * @return A predefined empty instance of `RenderEmoteDefinition`.
     */
    override fun empty() = RenderEmoteDefinition.EMPTY
}