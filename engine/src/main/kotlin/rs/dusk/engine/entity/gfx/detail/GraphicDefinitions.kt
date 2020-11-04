package rs.dusk.engine.entity.gfx.detail

import rs.dusk.cache.definition.data.GraphicDefinition
import rs.dusk.cache.definition.decoder.GraphicDecoder
import rs.dusk.engine.entity.DefinitionsDecoder

class GraphicDefinitions(
    override val decoder: GraphicDecoder,
    override val extras: Map<String, Map<String, Any>>,
    override val names: Map<Int, String>
) : DefinitionsDecoder<GraphicDefinition, GraphicDecoder>