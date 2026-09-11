package content.area.tirannwn.isafdar

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.type.Tile

class UndergroundPassEntrance : Script {

    init {
        // Pass-through until the Underground Pass dungeon is implemented
        objectOperate("Enter", "isafdar_cave_entrance") {
            message("You enter the cave and follow the passage through the mountain...")
            delay(2)
            tele(Tile(2438, 3315))
        }

        objectOperate("Enter", "underground_pass_cave_entrance") {
            message("You enter the cave and follow the passage through the mountain...")
            delay(2)
            tele(Tile(2312, 3217))
        }
    }
}
