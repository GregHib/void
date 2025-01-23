package world.gregs.voidps.world.map.lumbridge

import world.gregs.voidps.engine.entity.character.animDelay
import world.gregs.voidps.engine.entity.obj.objectOperate

objectOperate("Raise", "lumbridge_flag") {
    target.animate("lumbridge_flag")
    player.animDelay("lumbridge_flag_raise")
    player.animDelay("lumbridge_flag_stop_raise")
    player.say("All Hail the Duke!")
    player.animDelay("emote_salute")
    player["raise_the_roof_task"] = true
}
