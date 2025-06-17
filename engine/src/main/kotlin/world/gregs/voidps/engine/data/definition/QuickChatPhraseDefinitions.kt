package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.data.QuickChatPhraseDefinition

class QuickChatPhraseDefinitions(
    override var definitions: Array<QuickChatPhraseDefinition>,
) : DefinitionsDecoder<QuickChatPhraseDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = QuickChatPhraseDefinition.EMPTY

    fun load(): QuickChatPhraseDefinitions = this
}
