package content.area.misthalin.varrock.palace

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.type.Direction

class VarrockManhole : Script {

    init {
        objectOperate("Open", "varrock_manhole") { (target) ->
            anim("open_manhole")
            delay(2)
            target.replace("varrock_manhole_open")
            message("You pull back the cover from over the manhole.")
            sound("coffin_open")
        }

        objTeleportTakeOff("Climb-down", "varrock_manhole_open") { _, _ ->
            anim("mah4_reach_for_rampart_top")
            return@objTeleportTakeOff 1
        }

        objTeleportLand("Climb-down", "varrock_manhole_open") { _, _ ->
            face(Direction.EAST)
            animDelay("human_get_off_ladder_in_square")
        }

        objectOperate("Close", "varrock_manhole_open") { (target) ->
            target.replace("varrock_manhole")
            message("You place the cover back over the manhole.")
        }
    }
}
