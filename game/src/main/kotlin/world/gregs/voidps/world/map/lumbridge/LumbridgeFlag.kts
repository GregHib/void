package world.gregs.voidps.world.map.lumbridge

import world.gregs.voidps.engine.entity.character.forceChat
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.suspend.playAnimation

objectOperate("Raise", "lumbridge_flag") {
    target.animate("lumbridge_flag")
    player.playAnimation("lumbridge_flag_raise")
    player.playAnimation("lumbridge_flag_stop_raise")
    player.forceChat = "All Hail the Duke!"
    player.playAnimation("emote_salute")
    player["raise_the_roof_task"] = true
}
