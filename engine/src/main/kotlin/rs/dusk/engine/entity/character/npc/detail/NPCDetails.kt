package rs.dusk.engine.entity.character.npc.detail

import com.google.common.collect.BiMap
import rs.dusk.engine.entity.EntityDetails

class NPCDetails(
    override val details: Map<Int, NPCDetail>,
    override val names: BiMap<Int, String>
) : EntityDetails<NPCDetail> {

    override fun getOrNull(id: Int): NPCDetail? {
        return details[id]
    }

    override fun get(id: Int): NPCDetail {
        return getOrNull(id) ?: NPCDetail(id)
    }

}