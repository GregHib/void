package content.skill.construction

import content.skill.construction.House.Companion.notImplemented
import world.gregs.voidps.engine.Script

/**
 * Dungeon furniture: locked doors between rooms, oubliette ladders and treasure room chests
 */
class HouseDungeon : Script {
    init {
        objectOperate("Pick-lock", DOORS) {
            notImplemented()
        }

        objectOperate("Force", DOORS) {
            notImplemented()
        }

        objectOperate("Climb", "oak_ladder,teak_ladder,mahogany_ladder") {
            notImplemented()
        }

        objectOperate("Open", "wooden_treasure_crate,oak_treasure_room_chest,teak_treasure_room_chest,mahogany_treasure_room_chest,magic_treasure_room_chest") {
            notImplemented()
        }
    }

    companion object {
        private const val DOORS = "door_309_closed,door_310_closed,door_311_closed,door_312_closed,door_313_closed,door_314_closed"
    }
}
