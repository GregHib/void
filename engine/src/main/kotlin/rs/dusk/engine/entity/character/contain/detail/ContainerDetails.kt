package rs.dusk.engine.entity.character.contain.detail

import com.google.common.collect.BiMap
import rs.dusk.engine.entity.EntityDetails

class ContainerDetails(
    override val details: Map<Int, ContainerDetail>,
    override val names: BiMap<Int, String>
) : EntityDetails<ContainerDetail> {

    override fun getOrNull(id: Int): ContainerDetail? {
        return details[id]
    }

    override fun get(id: Int): ContainerDetail {
        return getOrNull(id) ?: ContainerDetail(id)
    }

}