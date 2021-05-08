import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.character.get
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.set
import world.gregs.voidps.engine.entity.character.update.visual.clearAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.world.interact.entity.player.music.play

val animations = setOf(
    "rest_arms_back",
    "rest_arms_crossed",
    "rest_legs_out"
)

on<InterfaceOption>({ name == "energy_orb" && option == "Rest" }) { player: Player ->
    rest(player, -1)
}

on<NPCOption>({ npc.def["song", -1] != -1 && option == "Listen-to" }) { player: Player ->
    rest(player, npc.def["song"])
}

fun rest(player: Player, music: Int) {
    println(player.action.type)
    if (player.action.type == ActionType.Resting) {
        player.message("You are already resting.")
        return
    }
    player.action(ActionType.Resting) {
        player.movement.clear()
        player["movement"] = player.getVar("movement", "walk")
        val anim = animations.random()
        val lastTrack = player["current_track", -1]
        try {
            player.setVar("movement", if (music != -1) "music" else "rest")
            player.setAnimation(anim)
            if (music != -1) {
                player.play(music)
            } else {
                player.message("You begin resting..", ChatType.GameFilter)
            }
            await(Suspension.Infinite)
        } finally {
            player.setAnimation(anim.replace("rest", "stand"))
            val type = player["movement", "walk"]
            player.setVar("movement", type)
            if (lastTrack != -1) {
                player.play(lastTrack)
            }
            player.movement.frozen = true
            delay(player, if (type == "run") 2 else 3) {
                player.clearAnimation()
                player.movement.frozen = false
            }
        }
    }
}