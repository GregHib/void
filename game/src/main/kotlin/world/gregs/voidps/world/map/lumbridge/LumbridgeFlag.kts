package world.gregs.voidps.world.map.lumbridge

import world.gregs.voidps.engine.entity.character.forceChat
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.suspend.arriveDelay
import world.gregs.voidps.engine.suspend.playAnimation

on<ObjectOption>({ operate && target.id == "lumbridge_flag" && option == "Raise" }) { player: Player ->
    arriveDelay()
    player.strongQueue("lumbridge_flag") {
        target.animate("lumbridge_flag")
        player.playAnimation("lumbridge_flag_raise")
        player.playAnimation("lumbridge_flag_stop_raise")
        player.forceChat = "All Hail the Duke!"
        player.playAnimation("emote_salute")
    }
}
