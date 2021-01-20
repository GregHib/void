package world.gregs.void.engine.entity.definition

import world.gregs.void.cache.definition.data.NPCDefinition
import world.gregs.void.cache.definition.decoder.NPCDecoder

class NPCDefinitions(
    override val decoder: NPCDecoder,
    override val extras: Map<String, Map<String, Any>>,
    override val names: Map<Int, String>
) : DefinitionsDecoder<NPCDefinition, NPCDecoder>