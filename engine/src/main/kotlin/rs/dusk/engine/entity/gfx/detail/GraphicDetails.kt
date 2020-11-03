package rs.dusk.engine.entity.gfx.detail

import rs.dusk.cache.definition.data.GraphicDefinition
import rs.dusk.cache.definition.decoder.GraphicDecoder
import rs.dusk.engine.entity.DetailsDecoder

class GraphicDetails(
    override val decoder: GraphicDecoder,
    override val details: Map<String, Map<String, Any>>,
    override val names: Map<Int, String>
) : DetailsDecoder<GraphicDefinition, GraphicDecoder>