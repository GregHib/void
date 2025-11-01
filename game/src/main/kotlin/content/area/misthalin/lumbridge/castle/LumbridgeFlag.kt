package content.area.misthalin.lumbridge.castle

import world.gregs.voidps.engine.Script

class LumbridgeFlag : Script {

    init {
        objectOperate("Raise", "lumbridge_flag") { (target) ->
            target.anim("lumbridge_flag")
            animDelay("lumbridge_flag_raise")
            animDelay("lumbridge_flag_stop_raise")
            say("All Hail the Duke!")
            animDelay("emote_salute")
            set("raise_the_roof_task", true)
        }
    }
}
