package content.entity.world.music

import world.gregs.voidps.type.Area

data class Track(val id: Int, val name: String, val index: Int, val area: Area? = null)