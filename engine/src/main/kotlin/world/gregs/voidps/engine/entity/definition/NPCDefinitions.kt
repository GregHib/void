package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.decoder.NPCDecoder

class NPCDefinitions(
    override val decoder: NPCDecoder,
    override val extras: Map<String, Map<String, Any>>,
    override val names: Map<Int, String>
) : DefinitionsDecoder<NPCDefinition, NPCDecoder>