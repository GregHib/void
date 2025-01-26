package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.data.AnimationDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

/**
 * A class responsible for managing and decoding animation definitions.
 * It extends the [DefinitionsDecoder] interface, which provides mechanisms to decode and handle definitions.
 *
 * @property definitions An array of [AnimationDefinition] instances that represent animation configurations.
 */
class AnimationDefinitions(
    override var definitions: Array<AnimationDefinition>
) : DefinitionsDecoder<AnimationDefinition> {

    /**
     * A map that stores identifier keys as strings and their corresponding integer values.
     * This variable is overridden and will be initialized later with a proper value.
     */
    override lateinit var ids: Map<String, Int>

    /**
     * Indicates that the current animation definition is empty.
     * This method is often used as a placeholder or default value
     * when no specific animation definition is provided.
     *
     * @return An empty animation definition instance.
     */
    override fun empty() = AnimationDefinition.EMPTY

    /**
     * Loads animation definitions from the specified YAML file using the provided path.
     *
     * @param yaml The Yaml instance to use for decoding. Defaults to the result of the `get()` method.
     * @param path The path to the definitions in the settings. Defaults to the value of `Settings["definitions.animations"]`.
     * @return The loaded animation definitions.
     */
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.animations"]): AnimationDefinitions {
        timedLoad("animation extra") {
            decode(yaml, path)
        }
        return this
    }

}