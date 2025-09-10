package content.area.misthalin.lumbridge.castle

import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.event.Script
@Script
class LumbridgeFlag {

    init {
        objectOperate("Raise", "lumbridge_flag") {
            target.anim("lumbridge_flag")
            player.animDelay("lumbridge_flag_raise")
            player.animDelay("lumbridge_flag_stop_raise")
            player.say("All Hail the Duke!")
            player.animDelay("emote_salute")
            player["raise_the_roof_task"] = true
        }

    }

}
