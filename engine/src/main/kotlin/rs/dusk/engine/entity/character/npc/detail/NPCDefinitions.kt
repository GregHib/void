package rs.dusk.engine.entity.character.npc.detail

import rs.dusk.cache.definition.data.NPCDefinition
import rs.dusk.cache.definition.decoder.NPCDecoder
import rs.dusk.engine.entity.DefinitionsDecoder

class NPCDefinitions(
    override val decoder: NPCDecoder,
    override val extras: Map<String, Map<String, Any>>,
    override val names: Map<Int, String>
) : DefinitionsDecoder<NPCDefinition, NPCDecoder>