package world.gregs.voidps.engine.data.definition.extra

import world.gregs.voidps.cache.definition.data.QuickChatPhraseDefinition
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder

class QuickChatPhraseDefinitions(
    override var definitions: Array<QuickChatPhraseDefinition>
) : DefinitionsDecoder<QuickChatPhraseDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = QuickChatPhraseDefinition.EMPTY

    fun load(): QuickChatPhraseDefinitions {
        return this
    }

}