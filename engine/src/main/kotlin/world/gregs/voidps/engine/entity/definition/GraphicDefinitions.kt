package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.definition.data.GraphicDefinition
import world.gregs.voidps.cache.definition.decoder.GraphicDecoder

class GraphicDefinitions(
    override val decoder: GraphicDecoder,
    override val extras: Map<String, Map<String, Any>>,
    override val names: Map<Int, String>
) : DefinitionsDecoder<GraphicDefinition, GraphicDecoder>