import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.npc.NPCClick
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.player.music.playTrack

val animations = setOf(
    "rest_arms_back",
    "rest_arms_crossed",
    "rest_legs_out"
)
val alreadyResting = "You are already resting."

on<InterfaceOption>({ id == "energy_orb" && option == "Rest" }) { player: Player ->
    if (player.getVar("movement", "walk") == "rest") {
        player.message(alreadyResting)
    } else {
        rest(player, -1)
    }
}

on<NPCClick>({ option == "Listen-to" }) { player: Player ->
    if (player.getVar("movement", "walk") == "music") {
        player.message(alreadyResting)
        cancel()
    }
}

on<NPCOption>({ def["song", -1] != -1 && option == "Listen-to" }) { player: Player ->
    rest(player, def["song"])
}

fun rest(player: Player, track: Int) {
    player.action(ActionType.Resting) {
        player.movement.clear()
        player["movement"] = player.getVar("movement", "walk")
        val anim = animations.random()
        val lastTrack = player["current_track", -1]
        try {
            player.setVar("movement", if (track != -1) "music" else "rest")
            player.setAnimation(anim)
            if (track != -1) {
                player.playTrack(track)
            }
            await(Suspension.Infinite)
        } finally {
            withContext(NonCancellable) {
                val type = player["movement", "walk"]
                player.setVar("movement", type)
                if (lastTrack != -1) {
                    player.playTrack(lastTrack)
                }
                player.playAnimation(anim.replace("rest", "stand"))
                player.clearAnimation()
            }
        }
    }
}