package world.gregs.voidps.engine.data.types.keys

import world.gregs.voidps.cache.type.NpcType
import world.gregs.voidps.engine.data.param.Parameters
import world.gregs.voidps.engine.data.param.codec.IntParam

object NpcParams : Parameters<NpcType>() {

    override val parameters = mapOf(
        SOUND_MIN to IntParam(SOUND_MIN),
    )

    override val keys = mapOf(
        "clone" to CLONE,
        "id" to ID,
        "sound_min" to SOUND_MIN,
    )

    const val SOUND_MIN = 0

}


