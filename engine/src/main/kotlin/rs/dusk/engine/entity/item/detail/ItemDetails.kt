package rs.dusk.engine.entity.item.detail

import rs.dusk.cache.definition.data.ItemDefinition
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.entity.DetailsDecoder

class ItemDetails(
    override val decoder: ItemDecoder,
    override val details: Map<String, Map<String, Any>>,
    override val names: Map<Int, String>
) : DetailsDecoder<ItemDefinition, ItemDecoder>