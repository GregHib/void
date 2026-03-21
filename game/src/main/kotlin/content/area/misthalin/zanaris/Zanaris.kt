package content.area.misthalin.zanaris

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.type.Tile

class Zanaris : Script {
    init {
        objectOperate("Use", "fairy_ring_zanaris") { (target) ->
            walkOverDelay(target.tile)
            Teleport.teleport(this, Tile(3201, 3169), "fairy")
        }

        objectOperate("Enter", "zanaris_crop_circle") {
            walkToDelay(Tile(2427, 4446))
            Teleport.teleport(this, Tile(2591, 4319), "puro_puro")
        }

        objectOperate("Exit", "puro_puro_exit") {
            walkToDelay(Tile(2592, 4320))
            Teleport.teleport(this, Tile(2591, 4319), "puro_puro")
        }

        objectOperate("Quick-leave", "puro_puro_exit") {
            tele(2426, 4445) // TODO check how this works
        }
    }
}
