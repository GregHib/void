package rs.dusk.engine.entity.character.npc.detail

import rs.dusk.cache.definition.data.NPCDefinition
import rs.dusk.cache.definition.decoder.NPCDecoder
import rs.dusk.engine.entity.DetailsDecoder

class NPCDetails(
    override val decoder: NPCDecoder,
    override val details: Map<String, Map<String, Any>>,
    override val names: Map<Int, String>
) : DetailsDecoder<NPCDefinition, NPCDecoder>