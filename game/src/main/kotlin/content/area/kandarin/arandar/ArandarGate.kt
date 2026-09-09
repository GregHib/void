package content.area.kandarin.arandar

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile

class ArandarGate : Script {

    init {
        objectOperate("Enter", "arandar_heavy_gate,arandar_heavy_gate_2") { (target) ->
            // Each half swings across to the opposite side and closes itself a few ticks later
            if (target.id == "arandar_heavy_gate") {
                GameObjects.replace(target, "arandar_heavy_gate_open", tile = Tile(2384, 3333, target.tile.level), rotation = 3, ticks = 5)
            } else {
                GameObjects.replace(target, "arandar_heavy_gate_2_open", tile = Tile(2387, 3333, target.tile.level), rotation = 1, ticks = 5)
            }
            sound("bigdoor_open")
            val x = tile.x.coerceIn(target.tile.x, target.tile.x + target.width - 1)
            val dest = Tile(x, if (tile.y > target.tile.y) target.tile.y - 1 else target.tile.y + 1, tile.level)
            walkOverDelay(dest)
        }
    }
}
