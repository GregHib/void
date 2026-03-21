package content.area.misthalin.zanaris

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.type.Tile

class Zanaris : Script {
    init {
        objectOperate("Use", "fairy_ring_zanaris") { (target) ->
            walkOverDelay(target.tile)
            Teleport.teleport(this, Tile(3201, 3169), "fairy")
        }
    }
}
