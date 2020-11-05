package rs.dusk.engine.entity.definition

import rs.dusk.cache.definition.data.ItemDefinition
import rs.dusk.cache.definition.decoder.ItemDecoder

class ItemDefinitions(
    override val decoder: ItemDecoder,
    override val extras: Map<String, Map<String, Any>>,
    override val names: Map<Int, String>
) : DefinitionsDecoder<ItemDefinition, ItemDecoder>