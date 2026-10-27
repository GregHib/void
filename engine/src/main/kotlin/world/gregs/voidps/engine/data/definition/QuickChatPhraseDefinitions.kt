package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.data.QuickChatPhraseDefinition
import world.gregs.voidps.cache.definition.decoder.QuickChatPhraseDecoder
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.load.SnapshotCodec
import world.gregs.voidps.engine.data.snapshot.SnapshotReader
import world.gregs.voidps.engine.data.snapshot.SnapshotWriter

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
    override val codec = object : SnapshotCodec<Array<QuickChatPhraseDefinition>> {
        override val schema = 1L

        override fun write(writer: SnapshotWriter, value: Array<QuickChatPhraseDefinition>) = with(writer) {
            int(value.size)
            for (definition in value) {
                int(definition.id)
                nullableStringArray(definition.stringParts)
                nullableInts(definition.responses)
                nullableIntArrays(definition.ids)
                nullableInts(definition.types)
                string(definition.stringId)
                params(definition.params)
            }
        }

        override fun read(reader: SnapshotReader): Array<QuickChatPhraseDefinition> = Array(reader.int()) {
            QuickChatPhraseDefinition(
                id = reader.int(),
                stringParts = reader.nullableStringArray(),
                responses = reader.nullableInts(),
                ids = reader.nullableIntArrays(),
                types = reader.nullableInts(),
                stringId = reader.string(),
                params = reader.params(),
            )
        }
    }

    override fun load(context: LoadContext): Array<QuickChatPhraseDefinition> = QuickChatPhraseDecoder().load(context.requireCache())

    override fun install(state: Array<QuickChatPhraseDefinition>) {
        instance.definitions = state
    }
}
