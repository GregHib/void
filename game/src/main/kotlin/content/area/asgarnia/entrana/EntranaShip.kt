package content.area.asgarnia.entrana

import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.type.Tile

@Script
class EntranaShip {
    init {
        objectOperate("Cross", "entrana_gangplank_exit") {
            player.walkOverDelay(Tile(2834, 3333, 1))
            player.tele(2834, 3335, 0)
        }

        objectOperate("Cross", "gangplank_entrana_enter") {
            player.walkOverDelay(Tile(2834, 3334))
            player.tele(2834, 3332, 1)
        }
    }
}
