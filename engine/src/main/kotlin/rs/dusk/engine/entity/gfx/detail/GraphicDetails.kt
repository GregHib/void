package rs.dusk.engine.entity.gfx.detail

import com.google.common.collect.BiMap
import rs.dusk.engine.entity.EntityDetails

class GraphicDetails(
    override val details: Map<Int, GraphicDetail>,
    override val names: BiMap<Int, String>
) : EntityDetails {

    override fun getOrNull(id: Int): GraphicDetail? {
        return details[id]
    }

    override fun get(id: Int): GraphicDetail {
        return getOrNull(id) ?: GraphicDetail(id)
    }

}