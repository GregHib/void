package content.area.wilderness

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.type.Tile

class WildernessVolcano : Script {

    init {
        objectOperate("Enter", "wilderness_volcano_entrance") {
            tele(Tile(3164, 3696))
        }

        objectOperate("Exit", "wilderness_volcano_exit") {
            tele(Tile(3164, 3685))
        }
    }
}
