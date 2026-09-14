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
                GameObjects.replace(target, "arandar_heavy_gate_2_open", tile = Tile(2387, 3333, target.tile.level), rotation = 2, ticks = 5)
            } else {
                GameObjects.replace(target, "arandar_heavy_gate_open", tile = Tile(2384, 3333, target.tile.level), rotation = 2, ticks = 5)
            }
            sound("bigdoor_open")
            // Only the middle two tiles are a real opening; the rest would clip through the gate
            val x = tile.x.coerceIn(2385, 2386)
            val dest = Tile(x, if (tile.y > target.tile.y) target.tile.y - 1 else target.tile.y + 1, tile.level)
            walkOverDelay(dest)
        }
    }
}
