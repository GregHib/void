package rs.dusk.engine.entity.anim.detail

import com.google.common.collect.BiMap
import rs.dusk.engine.entity.EntityDetails

class AnimationDetails(
    override val details: Map<Int, AnimationDetail>,
    override val names: BiMap<Int, String>
) : EntityDetails {

    override fun getOrNull(id: Int): AnimationDetail? {
        return details[id]
    }

    override fun get(id: Int): AnimationDetail {
        return getOrNull(id) ?: AnimationDetail(id)
    }

}