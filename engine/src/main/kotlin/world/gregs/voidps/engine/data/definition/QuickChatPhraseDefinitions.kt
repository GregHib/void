package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.data.QuickChatPhraseDefinition

/**
 * Represents a collection of quick chat phrase definitions.
 * This class is responsible for decoding and managing quick chat phrase definitions.
 *
 * @property definitions The array of quick chat phrase definitions.
 */
class QuickChatPhraseDefinitions(
    override var definitions: Array<QuickChatPhraseDefinition>
) : DefinitionsDecoder<QuickChatPhraseDefinition> {

    /**
     * A map that stores key-value pairs where the key is a String and the value is an Integer.
     * This property is initialized lazily and can be overridden in subclasses.
     */
    override lateinit var ids: Map<String, Int>

    /**
     * Returns an empty instance of `QuickChatPhraseDefinition`.
     *
     * This method provides a predefined placeholder or default value, which is an instance
     * of `QuickChatPhraseDefinition` that represents an empty or uninitialized state.
     *
     * @return The empty instance of `QuickChatPhraseDefinition`.
     */
    override fun empty() = QuickChatPhraseDefinition.EMPTY

    /**
     * Loads the quick chat phrase definitions and returns them as a QuickChatPhraseDefinitions instance.
     *
     * @return the loaded QuickChatPhraseDefinitions instance.
     */
    fun load(): QuickChatPhraseDefinitions {
        return this
    }

}