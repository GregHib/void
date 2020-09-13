package rs.dusk.engine.entity.item.detail

import com.google.common.collect.BiMap
import rs.dusk.engine.entity.EntityDetails

class ItemDetails(
    override val details: Map<Int, ItemDetail>,
    override val names: BiMap<Int, String>
) : EntityDetails<ItemDetail> {

    override fun getOrNull(id: Int): ItemDetail? {
        return details[id]
    }

    override fun get(id: Int): ItemDetail {
        return getOrNull(id) ?: ItemDetail(id)
    }

}