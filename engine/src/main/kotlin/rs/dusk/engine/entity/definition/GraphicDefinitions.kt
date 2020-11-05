package rs.dusk.engine.entity.definition

import rs.dusk.cache.definition.data.GraphicDefinition
import rs.dusk.cache.definition.decoder.GraphicDecoder

class GraphicDefinitions(
    override val decoder: GraphicDecoder,
    override val extras: Map<String, Map<String, Any>>,
    override val names: Map<Int, String>
) : DefinitionsDecoder<GraphicDefinition, GraphicDecoder>