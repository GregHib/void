package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.data.QuickChatPhraseDefinition
import world.gregs.voidps.cache.definition.decoder.QuickChatPhraseDecoder
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.snapshot.DefinitionArraySerializer
import world.gregs.voidps.engine.data.snapshot.SnapshotFormat

class QuickChatPhraseDefinitions(
    override var definitions: Array<QuickChatPhraseDefinition>,
) : DefinitionsDecoder<QuickChatPhraseDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = QuickChatPhraseDefinition.EMPTY

    fun load(): QuickChatPhraseDefinitions = this
}

object QuickChatPhraseLoader : Loader<Array<QuickChatPhraseDefinition>> {
    val instance = QuickChatPhraseDefinitions(emptyArray())
    override val name = "quick_chat"
    override val usesCache = true
    override val codec = SnapshotFormat.codec(DefinitionArraySerializer(QuickChatPhraseDefinition::class, QuickChatPhraseDefinition.serializer()))

    override fun load(context: LoadContext): Array<QuickChatPhraseDefinition> = QuickChatPhraseDecoder().load(context.requireCache())

    override fun install(state: Array<QuickChatPhraseDefinition>) {
        instance.definitions = state
    }
}
