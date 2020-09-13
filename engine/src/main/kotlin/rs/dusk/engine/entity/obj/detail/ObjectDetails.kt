package rs.dusk.engine.entity.obj.detail

import com.google.common.collect.BiMap
import rs.dusk.engine.entity.EntityDetails

class ObjectDetails(
    override val details: Map<Int, ObjectDetail>,
    override val names: BiMap<Int, String>
) : EntityDetails<ObjectDetail> {

    override fun getOrNull(id: Int): ObjectDetail? {
        return details[id]
    }

    override fun get(id: Int): ObjectDetail {
        return getOrNull(id) ?: ObjectDetail(id)
    }

}