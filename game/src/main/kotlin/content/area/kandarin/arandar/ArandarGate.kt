package content.area.kandarin.arandar

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.type.Tile

class ArandarGate : Script {

    init {
        objectOperate("Enter", "arandar_heavy_gate,arandar_heavy_gate_2") { (target) ->
            val dest = Tile(target.tile.x, if (tile.y > target.tile.y) target.tile.y - 1 else target.tile.y + 1, tile.level)
            delay()
            tele(dest)
        }
    }
}
