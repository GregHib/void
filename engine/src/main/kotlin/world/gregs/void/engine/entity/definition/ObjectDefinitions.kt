package world.gregs.void.engine.entity.definition

import world.gregs.void.cache.definition.data.ObjectDefinition
import world.gregs.void.cache.definition.decoder.ObjectDecoder

class ObjectDefinitions(
    override val decoder: ObjectDecoder,
    override val extras: Map<String, Map<String, Any>>,
    override val names: Map<Int, String>
) : DefinitionsDecoder<ObjectDefinition, ObjectDecoder>