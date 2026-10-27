package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.data.QuickChatPhraseDefinition
import world.gregs.voidps.engine.data.snapshot.Snapshot

class QuickChatPhraseDefinitions(
    override var definitions: Array<QuickChatPhraseDefinition>,
) : DefinitionsDecoder<QuickChatPhraseDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = QuickChatPhraseDefinition.EMPTY

    fun load(): QuickChatPhraseDefinitions = this

    fun save(snapshot: Snapshot) = snapshot.write {
        int(definitions.size)
        for (definition in definitions) {
            int(definition.id)
            nullableStringArray(definition.stringParts)
            nullableInts(definition.responses)
            nullableIntArrays(definition.ids)
            nullableInts(definition.types)
            string(definition.stringId)
            params(definition.params)
        }
    }

    companion object {
        fun restore(snapshot: Snapshot): QuickChatPhraseDefinitions? {
            var phrases: QuickChatPhraseDefinitions? = null
            snapshot.restore { reader ->
                phrases = QuickChatPhraseDefinitions(
                    Array(reader.int()) {
                        QuickChatPhraseDefinition(
                            id = reader.int(),
                            stringParts = reader.nullableStringArray(),
                            responses = reader.nullableInts(),
                            ids = reader.nullableIntArrays(),
                            types = reader.nullableInts(),
                            stringId = reader.string(),
                            params = reader.params(),
                        )
                    },
                )
            }
            return phrases
        }
    }
}
