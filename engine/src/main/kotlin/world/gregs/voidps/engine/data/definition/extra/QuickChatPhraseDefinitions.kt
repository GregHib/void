package world.gregs.voidps.engine.data.definition.extra

import world.gregs.voidps.cache.definition.data.QuickChatPhraseDefinition
import world.gregs.voidps.cache.definition.decoder.QuickChatPhraseDecoder
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder
import world.gregs.voidps.engine.timedLoad

class QuickChatPhraseDefinitions(
    decoder: QuickChatPhraseDecoder
) : DefinitionsDecoder<QuickChatPhraseDefinition> {

    override lateinit var definitions: Array<QuickChatPhraseDefinition>
    override lateinit var ids: Map<String, Int>

    init {
        val start = System.currentTimeMillis()
        definitions = decoder.indices.map { decoder.get(it) }.toTypedArray()
        timedLoad("quick chat phrase definition", definitions.size, start)
    }

    override fun empty() = QuickChatPhraseDefinition.EMPTY

    fun load(): QuickChatPhraseDefinitions {
        return this
    }

}