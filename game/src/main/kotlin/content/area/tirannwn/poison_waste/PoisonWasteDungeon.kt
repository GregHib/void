package content.area.tirannwn.poison_waste

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.equals

class PoisonWasteDungeon : Script {

    init {
        objectOperate("Enter", "poison_waste_cave_entrance,poison_waste_cave_entrance_2,poison_waste_cave_entrance_3") {
            message("You enter the murky cave...")
            delay(2)
            tele(Tile(1989, 4174))
        }

        objectOperate("Exit", "poison_waste_cave_exit,poison_waste_cave_exit_2,poison_waste_cave_exit_3,poison_waste_cave_exit_4") {
            delay(2)
            tele(Tile(2321, 3100))
        }

        objectOperate("Climb-up", "poison_waste_sewer_ladder_up") { (target) ->
            anim("climb_up")
            delay(2)
            val dest = if (target.tile.equals(2041, 4189) || target.tile.equals(2041, 4172)) {
                target.tile.add(1, 0, 1)
            } else {
                target.tile.add(0, 1, 1)
            }
            tele(dest)
        }

        objectOperate("Climb-down", "poison_waste_sewer_ladder_down") { (target) ->
            anim("climb_down")
            delay(2)
            val dest = if (target.tile.equals(2041, 4189) || target.tile.equals(2041, 4172)) {
                target.tile.add(-1, 0, -1)
            } else {
                target.tile.add(0, -1, -1)
            }
            tele(dest)
        }
    }
}
