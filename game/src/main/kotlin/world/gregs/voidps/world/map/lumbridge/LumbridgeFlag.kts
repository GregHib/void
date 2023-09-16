package world.gregs.voidps.world.map.lumbridge

import world.gregs.voidps.engine.entity.character.forceChat
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.suspend.arriveDelay

on<ObjectOption>({ operate && target.id == "lumbridge_flag" && option == "Raise" }) { player: Player ->
    arriveDelay()
    player.setAnimation("lumbridge_flag_raise")
    target.animate("lumbridge_flag")
    player.softQueue("lumbridge_flag", 8) {
        player.setAnimation("lumbridge_flag_stop_raise")
        player.softQueue("salute", 2) {
            player.forceChat = "All Hail the Duke!"
            player.setAnimation("emote_salute")
        }
    }
}
