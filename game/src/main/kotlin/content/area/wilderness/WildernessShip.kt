package content.area.wilderness

import world.gregs.voidps.engine.Script

class WildernessShip : Script {

    init {
        objTeleportTakeOff("Climb-up", "wilderness_ship_ladder_up_*") { _, _ ->
            1
        }

        objTeleportTakeOff("Climb-down", "wilderness_ship_ladder_down_*") { _, _ ->
            1
        }
    }
}
