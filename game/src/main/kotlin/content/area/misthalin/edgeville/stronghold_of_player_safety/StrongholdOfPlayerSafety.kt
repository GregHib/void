package content.area.misthalin.edgeville.stronghold_of_player_safety

import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.type.Tile

class StrongholdOfPlayerSafety : Script {

    init {
        playerSpawn {
            sendVariable("stronghold_of_player_safety_poster")
            sendVariable("stronghold_of_player_safety_lever")
        }

        objectOperate("Open", "misthalin_exam_door_closed") { (target) ->
            // From wiki: The main door into the Training Centre, which is locked, preventing players from opening it from the outside, until they talk to the Guard in the jail and go through the entire conversation about reporting rule-breakers
            if (get("safety_prison_guard_talked", false)) {
                enterDoor(target)
            } else {
                message("The door is locked.")
                return@objectOperate
            }
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
            animDelay("pull_ground_lever")
            target.anim("lever_down")
            areaSound("lever", target.tile)
            delay(2)
            areaSound("unlock", target.tile)
            delay(1)
            message("You hear cogs and gears moving and a distant unlocking sound.")
            set("stronghold_of_player_safety_lever", true)
        }

        objectOperate("Pull", "stronghold_of_player_safety_an_old_lever_opened") { (target) ->
            arriveDelay()
            animDelay("push_ground_lever")
            target.anim("lever_up")
            areaSound("lever", target.tile)
            delay(1)
            areaSound("unlock", target.tile)
            delay(1)
            message("You hear cogs and gears moving and the sound of heavy locks falling into place.")
            set("stronghold_of_player_safety_lever", false)
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
