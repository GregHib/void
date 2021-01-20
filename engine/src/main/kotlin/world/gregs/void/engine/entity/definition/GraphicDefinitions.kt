package world.gregs.void.engine.entity.definition

import world.gregs.void.cache.definition.data.GraphicDefinition
import world.gregs.void.cache.definition.decoder.GraphicDecoder

class GraphicDefinitions(
    override val decoder: GraphicDecoder,
    override val extras: Map<String, Map<String, Any>>,
    override val names: Map<Int, String>
) : DefinitionsDecoder<GraphicDefinition, GraphicDecoder>