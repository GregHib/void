package content.area.misthalin.edgeville.stronghold_of_player_safety

import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.type.Tile

class StrongholdOfPlayerSafety : Script {

    init {
        playerSpawn {
            sendVariable("stronghold_of_player_safety_poster")
            sendVariable("stronghold_of_player_safety_lever")
        }

        objectOperate("Use", "stronghold_of_player_safety_jail_entrance_down") {
            arriveDelay()
            tele(3082, 4229, 0)
            set("stronghold_of_player_safety_poster", true)
        }

        objectOperate("Pull-back", "player_safety_poster") { (target) ->
            walkToDelay(target.tile)
            statement("There appears to be a tunnel behind the poster.")
            tele(3140, 4230, 2)
        }

        objectOperate("Climb", "stronghold_of_player_safety_rope") { (target) ->
            walkToDelay(target.tile)
            set("safety_rope_climbed", true)
            tele(3077, 3462, 0)
        }
        objectOperate("Pull", "stronghold_of_player_safety_an_old_lever_closed") { (target) ->
            arriveDelay()
            anim("pull_ground_lever")
            target.anim("lever_down")
            areaSound("lever", target.tile)
            delay(2)
            areaSound("unlock", target.tile)
            delay(3)
            target.replace("stronghold_of_player_safety_an_old_lever_opened")
            set("stronghold_of_player_safety_lever", true)
            message("You hear cogs and gears moving and a distant unlocking sound.")
        }
        objectOperate("Pull", "stronghold_of_player_safety_an_old_lever_opened") { (target) ->
            arriveDelay()
            anim("push_ground_lever")
            target.anim("lever_up")
            areaSound("lever", target.tile)
            delay(1)
            areaSound("unlock", target.tile)
            delay(2)
            target.replace("stronghold_of_player_safety_an_old_lever_closed")
            set("stronghold_of_player_safety_lever", false)
            message("You hear cogs and gears moving and the sound of heavy locks falling into place.")
        }

        objectOperate("Enter", "stronghold_of_player_safety_crevice") {
            arriveDelay()
            if (get("safety_rope_climbed", false)) {
                tele(3157, 4279, 3)
            } else {
                message("You're not sure if you should go in there; perhaps you should find another way in.")
            }
        }
        objectOperate("Open", "stronghold_of_player_safety_jail_door_locked") { (target) ->
            arriveDelay()
            if (get("stronghold_of_player_safety_lever", false)) {
                when (target.tile) {
                    Tile(3178, 4266, 0) -> tele(3177, 4269, 2)
                    Tile(3178, 4269, 2) -> tele(3177, 4266, 0)
                    Tile(3142, 4270, 0) -> tele(3142, 4272, 1)
                    Tile(3141, 4272, 1) -> tele(3143, 4270, 0)
                }
            } else {
                message("The door seems to be locked by some kind of mechanism.")
            }
        }
    }
}
