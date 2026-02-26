package content.area.misthalin.edgeville.stronghold_of_player_safety

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.type.Tile

class StrongholdOfPlayerSafetyJailInterfaces : Script {
    init {
        objectOperate("Read-plaque on", "stronghold_of_player_safety_jail_door_2") { (target) ->
            when (target.tile) {
                Tile(3082, 4249, 0) -> moveCamera(Tile(3082, 4247), height = 300, speed = 10, acceleration = 20)
                Tile(3083, 4241, 0) -> moveCamera(Tile(3081, 4241), height = 300, speed = 10, acceleration = 20)
                Tile(3083, 4236, 0) -> moveCamera(Tile(3081, 4236), height = 300, speed = 10, acceleration = 20)
                Tile(3083, 4231, 0) -> moveCamera(Tile(3081, 4231), height = 300, speed = 10, acceleration = 20)
            }
            when (target.tile) {
                Tile(3082, 4249, 0) -> turnCamera(Tile(3082, 4247), height = 300, speed = 10, acceleration = 20)
                Tile(3083, 4241, 0) -> turnCamera(Tile(3083, 4241), height = 300, speed = 10, acceleration = 20)
                Tile(3083, 4236, 0) -> turnCamera(Tile(3083, 4236), height = 300, speed = 10, acceleration = 20)
                Tile(3083, 4231, 0) -> turnCamera(Tile(3083, 4231), height = 300, speed = 10, acceleration = 20)
            }
            delay(4)
            when (target.tile) {
                Tile(3082, 4249, 0) -> open("report_abuse_instructions")
                Tile(3083, 4241, 0) -> open("report_abuse_instructions_4")
                Tile(3083, 4236, 0) -> open("report_abuse_instructions_6")
                Tile(3083, 4231, 0) -> open("report_abuse_instructions_2")
            }
        }

        interfaceClosed("report_abuse_instructions*") {
            clearCamera()
        }

        objectOperate("Read-plaque on", "stronghold_of_player_safety_jail_door") { (target) ->
            when (target.tile) {
                Tile(3080, 4240, 0) -> moveCamera(Tile(3082, 4240), height = 300, speed = 10, acceleration = 20)
                Tile(3080, 4245, 0) -> moveCamera(Tile(3082, 4245), height = 300, speed = 10, acceleration = 20)
                Tile(3080, 4230, 0) -> moveCamera(Tile(3082, 4230), height = 300, speed = 10, acceleration = 20)
            }
            when (target.tile) {
                Tile(3080, 4240, 0) -> turnCamera(Tile(3080, 4240), height = 300, speed = 10, acceleration = 20)
                Tile(3080, 4245, 0) -> turnCamera(Tile(3080, 4245), height = 300, speed = 10, acceleration = 20)
                Tile(3080, 4230, 0) -> turnCamera(Tile(3080, 4230), height = 300, speed = 10, acceleration = 20)
            }
            delay(4)
            when (target.tile) {
                Tile(3080, 4240, 0) -> open("report_abuse_instructions_5")
                Tile(3080, 4245, 0) -> open("report_abuse_instructions_7")
                Tile(3080, 4230, 0) -> open("report_abuse_instructions_3")
            }
        }
    }
}
