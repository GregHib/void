package rs.dusk.engine.entity.obj.detail

import rs.dusk.cache.definition.data.ObjectDefinition
import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.engine.entity.DetailsDecoder

class ObjectDefinitions(
    override val decoder: ObjectDecoder,
    override val details: Map<String, Map<String, Any>>,
    override val names: Map<Int, String>
) : DetailsDecoder<ObjectDefinition, ObjectDecoder>